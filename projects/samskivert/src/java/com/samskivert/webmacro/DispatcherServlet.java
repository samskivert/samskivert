//
// $Id: DispatcherServlet.java,v 1.1 2001/02/15 01:44:34 mdb Exp $

package com.samskivert.webmacro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.samskivert.util.ConfigUtil;
import com.samskivert.util.StringUtil;
import org.webmacro.*;
import org.webmacro.servlet.*;

/**
 * The dispatcher servlet builds upon WebMacro's architecture. It does so
 * in the following ways:
 *
 * <ul>
 * <li> It defines the notion of a context populator which populates the
 * context with data to be used to satisfy a particular request. The
 * context populator is not a servlet and is therefore limited in what it
 * can do while populating data. Experience dictates that ultimate
 * flexibility leads to bad design decisions and that this is a place
 * where that sort of thing can be comfortably nipped in the bud. <br><br>
 *
 * <li> It allows .wm files to be referenced directly in the URL while
 * maintaining the ability to choose a cobranded template based. The URI
 * is mapped to a servlet based on some simple mapping rules. This
 * provides template designers with a clearer understanding of the
 * structure of a web application as well as with an easy way to test
 * their templates in the absence of an associated servlet. <br><br>
 *
 * <li> It provides a common error handling paradigm that simplifies the
 * task of authoring web applications.
 * </ul>
 *
 * <p><b>URI to servlet mapping</b><br>
 * The mapping process allows the webmacro framework to be invoked for all
 * requests ending in a particular file extension (usually
 * <code>.wm</code>). It is necessary to instruct your servlet engine of
 * choice to invoke the <code>Dispatcher</code> servlet for all requests
 * ending in that extension. For Apache/JServ this looks something like
 * this:
 *
 * <pre>
 * ApJServAction .wm /servlets/com.samskivert.webmacro.Dispatcher
 * </pre>
 *
 * The request URI then defines the path of the webmacro template that
 * will be used to satisfy the request. To understand how code is selected
 * to go along with the request, let's look at an example. Consider the
 * following configuration:
 *
 * <pre>
 * applications=whowhere
 * whowhere.base_uri=/whowhere
 * whowhere.base_pkg=whowhere.servlets
 * </pre>
 *
 * This defines an application identified as <code>whowhere</code>. An
 * application is defined by three parameters, the application identifier,
 * the <code>base_uri</code>, and the <code>base_pkg</code>. The
 * <code>base_uri</code> defines the prefix shared by all pages served by
 * the application and which serves to identify which application to
 * invoke when processing a request. The <code>base_pkg</code> is used to
 * construct the populator classname based on the URI and the
 * <code>base_uri</code> parameter.
 *
 * <p> Now let's look at a sample request to determine how the populator
 * classname is resolved. Consider the following request URI:
 *
 * <pre>
 * /whowhere/view/trips.wm
 * </pre>
 *
 * It begins with <code>/whowhere</code> which tells the dispatcher that
 * it's part of the <code>whowhere</code> application. That application's
 * <code>base_uri</code> is then stripped from the URI leaving
 * <code>/view/trips.wm</code>. The slashes are converted into periods to
 * map directories to packages, giving us <code>view.trips.wm</code>.
 * Finally, the <code>base_pkg</code> is prepended and the trailing
 * <code>.wm</code> extension removed.
 *
 * <p> Thus the class invoked to populate the context for this request is
 * <code>whowhere.servlets.view.trips</code> (note that the classname
 * <em>is</em> lowercase which is an intentional choice in resolving
 * conflicting recommendations that classnames should always start with a
 * capital letter and URLs should always be lowercase).
 *
 * <p> The template used to generate the result is loaded based on the
 * full URI, essentially with a call to
 * <code>getTemplate("/whowhere/view/trips.wm")</code> in this example.
 * This is the place where more sophisticated cobranding support could be
 * inserted in the future (ie. if I ever want to use this to develop a
 * cobranded web site).
 *
 * <p><b>Error handling</b><br>
 * The dispatcher servlet provides a common error handling mechanism. The
 * design is to catch any exceptions thrown by the populator and to
 * convert them into friendly error messages that are inserted into the
 * web context with the key <code>"error"</code> for easy display in the
 * resulting web page.
 *
 * <p> The process of mapping exceptions to friendly error messages is
 * done using the <code>ExceptionMap</code> class. Consult its
 * documentation for an explanation of how it works.
 *
 * @see ContextPopulator
 * @see ExceptionMap
 */
public class DispatcherServlet extends WMServlet
{
    public void start ()
	throws ServletException
    {
	// Log.log.info("Initializing dispatcher servlet.");

	// first load the properties file
	Properties props = null;
	try {
	    // load up our configuration
	    ClassLoader cld = DispatcherServlet.class.getClassLoader();
	    props = ConfigUtil.loadProperties("dispatcher.properties", cld);

	} catch (IOException ioe) {
	    Log.log.warning("Failure trying to load " +
			    "'dispatcher.properties': " + ioe);
	}

	if (props == null) {
	    Log.log.warning("Unable to load properties for " +
			    "dispatcher servlet.");
	    Log.log.warning("Make sure 'dispatcher.properties' file " +
			    "exists somewhere in your classpath.");
	    props = new Properties();
	}

	// now parse the configuration
	String apps = props.getProperty("applications");
	if (apps != null) {
	    StringTokenizer tok = new StringTokenizer(apps);
	    while (tok.hasMoreTokens()) {
		String appid = tok.nextToken();
		String baseURI = props.getProperty(appid + ".base_uri");
		String basePkg = props.getProperty(appid + ".base_pkg");

		// make sure we're not missing anything
		if (baseURI == null) {
		    Log.log.warning("Application '" + appid +
				    "' missing base_uri specification.");
		    continue;
		}
		if (basePkg == null) {
		    Log.log.warning("Application '" + appid +
				    "' missing base_pkg specification.");
		    continue;
		}

		// construct an application object and add it to our list
		_apps.add(new Application(baseURI, basePkg));
	    }
	}
    }

    public Template handle (WebContext ctx) throws HandlerException
    {
	// first we select the template
	Template tmpl;
	try {
	    tmpl = selectTemplate(ctx);
	} catch (NotFoundException e) {
	    throw new HandlerException("Unable to load template: " + e);
	}

	// then we populate the context with data
	try {
	    ContextPopulator pop = selectPopulator(ctx);
	    // if no populator is matched, we simply execute the template
	    // directly with the default information in the context (tools
	    // and other WebMacro services can be used by the template to
	    // do their WebMacro thing)
	    if (pop != null) {
		pop.populate(ctx);
	    }
	    
	} catch (DataValidationException dve) {
	    ctx.put(DV_ERROR_KEY, ExceptionMap.getMessage(dve));

	} catch (Exception e) {
	    ctx.put(ERROR_KEY, ExceptionMap.getMessage(e));
	}

	return tmpl;
    }

    /**
     * This method is called to select the appropriate template for this
     * request. The default implementation simply loads the template using
     * WebMacro's default template loading services based on the URI
     * provided in the request. It is assumed that the document root is
     * registered in WebMacro's search path.
     *
     * @param ctx The context of this request.
     *
     * @return The template to be used in generating the response.
     */
    protected Template selectTemplate (WebContext ctx)
	throws NotFoundException
    {
	String path = cleanupURI(ctx.getRequest().getRequestURI());
	// Log.log.info("Loading template [path=" + path + "].");
	return getTemplate(path);
    }

    /**
     * This method is called to select the appropriate context populator
     * for this request. The dispatcher configuration described in this
     * class's documentation is consulted to map the URI to a populator
     * class which is then instantiated and a single instance used to
     * process all matching requests.
     *
     * @param ctx The context of this request.
     *
     * @return The populator to be used in generating the response or null
     * if no populator could be matched.
     */
    protected ContextPopulator selectPopulator (WebContext ctx)
    {
	String path = cleanupURI(ctx.getRequest().getRequestURI());
	String pclass = null;
	// Log.log.info("Loading populator [path=" + path + "].");

	// try to locate an application that matches this URI
	for (int i = 0; i < _apps.size(); i++) {
	    Application app = (Application)_apps.get(i);
	    if (app.matches(path)) {
		pclass = app.generateClass(path);
		break;
	    }
	}

	// if we didn't find a matching application, we can stop now
	if (pclass == null) {
	    return null;
	}

	// otherwise look for a cached populator instance
	ContextPopulator pop = (ContextPopulator)_pops.get(pclass);
	if (pop == null) {
	    try {
		Class pcl = Class.forName(pclass);
		pop = (ContextPopulator)pcl.newInstance();

	    } catch (Throwable t) {
		Log.log.warning("Unable to instantiate populator for " +
				"matching application [path=" + path +
				", pclass=" + pclass + ", error=" + t + "].");
		// use a dummy in it's place so that we don't sit around
		// all day freaking out about our inability to instantiate
		// the proper populator class
		pop = new DummyPopulator();
	    }
	    _pops.put(pclass, pop);
	}

	return pop;
    }

    /**
     * Because the URI can actually contain the protocol and server name,
     * though it commonly doesn't, we need to be sure to remove that extra
     * information before we look at the URI because we only want the path
     * part of the URI. The JSDK unfortunately doesn't provide an easy
     * mechanism to request just the path part of the URI.
     */
    protected static String cleanupURI (String uri)
    {
	int dsidx = uri.indexOf("//");
	if (dsidx == -1) {
	    return uri;
	}

	int sidx = uri.indexOf("/", dsidx + 2);
	if (sidx == -1) {
	    Log.log.warning("Malformed URI?! [uri=" + uri + "].");
	    return "/";
	}

	return uri.substring(sidx);
    }

    public static void main (String[] args)
    {
	System.out.println(cleanupURI("/whowhere/viewtrip.wm"));
	System.out.println(cleanupURI("http://samskivert.com/foo/bar/baz.wm"));
	System.out.println(cleanupURI("http://samskivert.com"));
	System.out.println(cleanupURI("http://samskivert.com/"));
	System.out.println(cleanupURI("//samskivert.com/is/this/even/legal"));
    }

    protected static class Application
    {
	public Application (String baseURI, String basePkg)
	{
	    // remove any trailing slash
	    if (baseURI.endsWith("/")) {
		_baseURI = baseURI.substring(0, baseURI.length()-1);
	    } else {
		_baseURI = baseURI;
	    }

	    // remove any trailing dot
	    if (basePkg.endsWith(".")) {
		_basePkg = basePkg.substring(0, basePkg.length()-1);
	    } else {
		_basePkg = basePkg;
	    }
	}

	public boolean matches (String uri)
	{
	    return uri.startsWith(_baseURI);
	}

	public String generateClass (String uri)
	{
	    // remove the base URI
	    uri = uri.substring(_baseURI.length());
	    // convert slashes to dots
	    uri = StringUtil.replace(uri, "/", ".");
	    // remove the trailing file extension
	    uri = uri.substring(0, uri.length() - FILE_EXTENSION.length());
	    // prepend the base package and we're all set
	    return _basePkg + uri;
	}

	protected String _baseURI;
	protected String _basePkg;
    }

    protected ArrayList _apps = new ArrayList();
    protected HashMap _pops = new HashMap();

    /** This is the key used in the context for error messages. */
    protected static final String ERROR_KEY = "error";

    /**
     * This is the key used in the context for data validation error
     * messages.
     */
    protected static final String DV_ERROR_KEY = "invalid_data";

    /**
     * This is the default file extension.
     */
    protected static final String FILE_EXTENSION = ".wm";
}
