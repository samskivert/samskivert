//
// $Id: DispatcherServlet.java,v 1.7 2001/03/04 06:15:39 mdb Exp $

package com.samskivert.webmacro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.webmacro.*;
import org.webmacro.servlet.HandlerException;
import org.webmacro.servlet.WMServlet;
import org.webmacro.servlet.WebContext;

import com.samskivert.Log;
import com.samskivert.servlet.RedirectException;
import com.samskivert.util.ConfigUtil;
import com.samskivert.util.StringUtil;

/**
 * The dispatcher servlet builds upon WebMacro's architecture. It does so
 * in the following ways:
 *
 * <ul>
 * <li> It defines the notion of a logic object which populates the
 * context with data to be used to satisfy a particular request. The logic
 * is not a servlet and is therefore limited in what it can do while
 * populating data. Experience dictates that ultimate flexibility leads to
 * bad design decisions and that this is a place where that sort of thing
 * can be comfortably nipped in the bud. <br><br>
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
 * whowhere.base_pkg=whowhere.logic
 * </pre>
 *
 * This defines an application identified as <code>whowhere</code>. An
 * application is defined by three parameters, the application identifier,
 * the <code>base_uri</code>, and the <code>base_pkg</code>. The
 * <code>base_uri</code> defines the prefix shared by all pages served by
 * the application and which serves to identify which application to
 * invoke when processing a request. The <code>base_pkg</code> is used to
 * construct the logic classname based on the URI and the
 * <code>base_uri</code> parameter.
 *
 * <p> Now let's look at a sample request to determine how the logic
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
 * design is to catch any exceptions thrown by the logic and to convert
 * them into friendly error messages that are inserted into the web
 * context with the key <code>"error"</code> for easy display in the
 * resulting web page.
 *
 * <p> The process of mapping exceptions to friendly error messages is
 * done using the <code>ExceptionMap</code> class. Consult its
 * documentation for an explanation of how it works.
 *
 * @see Logic
 * @see ExceptionMap
 */
public class DispatcherServlet extends WMServlet
{
    public void start ()
	throws ServletException
    {
	// Log.info("Initializing dispatcher servlet.");

	// first load the properties file
	Properties props = null;
	try {
	    // load up our configuration
	    ClassLoader cld = DispatcherServlet.class.getClassLoader();
	    props = ConfigUtil.loadProperties("dispatcher.properties", cld);

	} catch (IOException ioe) {
	    Log.warning("Failure trying to load 'dispatcher.properties': " +
			ioe);
	}

	if (props == null) {
	    Log.warning("Unable to load properties for dispatcher servlet.");
	    Log.warning("Make sure 'dispatcher.properties' file exists " +
			"somewhere in your classpath.");
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
		String appcl = props.getProperty(appid + ".application");

		// make sure we're not missing anything
		if (baseURI == null) {
		    Log.warning("Application '" + appid + "' missing " +
				"base_uri specification.");
		    continue;
		}
		if (basePkg == null) {
		    Log.warning("Application '" + appid + "' missing " +
				"base_pkg specification.");
		    continue;
		}

                // instantiate the specified application class
                Application app;

                try {
                    // if a custom application class was specified,
                    // instantiate one of those. otherwise use the default
                    if (appcl != null) {
                        Class appclass = Class.forName(appcl);
                        app = (Application)appclass.newInstance();
                    } else {
                        app = new Application();
                    }

                    // now initialize the applicaiton
                    app.setConfig(baseURI, basePkg);
                    app.init(getServletContext());

                    // finally add it to our list
                    _apps.add(app);

                } catch (Throwable t) {
                    Log.warning("Error instantiating custom application " +
                                "[class=" + appcl + "].");
                    Log.logStackTrace(t);
                }
	    }
	}
    }

    public void stop ()
    {
	// shutdown our applications
	for (int i = 0; i < _apps.size(); i++) {
	    Application app = (Application)_apps.get(i);
	    app.shutdown();
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

	// assume an HTML response unless otherwise massaged by the logic
	ctx.getResponse().setContentType("text/html");

	// then we populate the context with data
	try {
            String path = cleanupURI(ctx.getRequest().getRequestURI());

            // select the proper application for the request
            Application app = selectApplication(path);
            if (app != null) {
                // insert the application into the web context in case the
                // logic or a tool wishes to make use of it
                ctx.put(APPLICATION_KEY, app);

                // resolve the appropriate logic class for this URI and
                // execute it if it exists
                Logic logic = resolveLogic(app, path);
                if (logic != null) {
                    logic.invoke(ctx);
                }
	    }

	} catch (RedirectException re) {
	    try {
		ctx.getResponse().sendRedirect(re.getRedirectURL());
                return null;
	    } catch (IOException ioe) {
		throw new HandlerException("Unable to send redirect: " + ioe);
	    }

	} catch (FriendlyException fe) {
	    ctx.put(ERROR_KEY, fe.getMessage());

	} catch (Exception e) {
	    ctx.put(ERROR_KEY, ExceptionMap.getMessage(e));
	    Log.logStackTrace(e);
	}

	return tmpl;
    }

    /**
     * Returns the reference to the application that is handling this
     * request.
     *
     * @return The application in effect for this request or null if no
     * application was selected to handle the request.
     */
    public static Application getApplication (WebContext context)
    {
        return (Application)context.get(APPLICATION_KEY);
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
	// Log.info("Loading template [path=" + path + "].");
	return getTemplate(path);
    }

    /**
     * Selects and returns the matching application for this request.
     *
     * @return the application that should handle this request or null if
     * no matching application could be found.
     */
    protected Application selectApplication (String path)
    {
	// try to locate an application that matches this URI
	for (int i = 0; i < _apps.size(); i++) {
	    Application app = (Application)_apps.get(i);
	    if (app.matches(path)) {
                return app;
	    }
	}

        return null;
    }

    /**
     * This method is called to select the appropriate logic for this
     * request URI.
     *
     * @return The logic to be used in generating the response or null if
     * no logic could be matched.
     */
    protected Logic resolveLogic (Application app, String path)
    {
	// look for a cached logic instance
	String lclass = app.generateClass(path);
	Logic logic = (Logic)_logic.get(lclass);

	if (logic == null) {
	    try {
		Class pcl = Class.forName(lclass);
		logic = (Logic)pcl.newInstance();

	    } catch (Throwable t) {
		Log.warning("Unable to instantiate logic for " +
			    "matching application [path=" + path +
			    ", lclass=" + lclass + ", error=" + t + "].");
		// use a dummy in it's place so that we don't sit around
		// all day freaking out about our inability to instantiate
		// the proper logic class
		logic = new DummyLogic();
	    }
	    _logic.put(lclass, logic);
	}

	return logic;
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
	    Log.warning("Malformed URI?! [uri=" + uri + "].");
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

    protected ArrayList _apps = new ArrayList();
    protected HashMap _logic = new HashMap();

    /** This is the key used in the context for error messages. */
    protected static final String ERROR_KEY = "error";

    /**
     * This is the key used to store a reference back to the dispatcher
     * servlet in our web context.
     */
    protected static final String APPLICATION_KEY = "%_app_%";
}
