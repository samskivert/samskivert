//
// $Id: DispatcherServlet.java,v 1.8 2001/11/02 18:46:48 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.velocity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.servlet.VelocityServlet;

import com.samskivert.Log;

import com.samskivert.servlet.MessageManager;
import com.samskivert.servlet.RedirectException;
import com.samskivert.servlet.util.ExceptionMap;
import com.samskivert.servlet.util.FriendlyException;

import com.samskivert.util.ConfigUtil;
import com.samskivert.util.StringUtil;

/**
 * The dispatcher servlet builds upon Velocity's architecture. It does so
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
 * <li> It allows template files to be referenced directly in the URL
 * while maintaining the ability to choose a cobranded template based on
 * information in the request. The URI is mapped to a servlet based on
 * some simple mapping rules. This provides template designers with a
 * clearer understanding of the structure of a web application as well as
 * with an easy way to test their templates in the absence of an
 * associated servlet. <br><br>
 *
 * <li> It provides a common error handling paradigm that simplifies the
 * task of authoring web applications.
 * </ul>
 *
 * <p><b>URI to servlet mapping</b><br>
 * The mapping process allows the Velocity framework to be invoked for all
 * requests ending in a particular file extension (usually
 * <code>.wm</code>). It is necessary to instruct your servlet engine of
 * choice to invoke the <code>DispatcherServlet</code> for all requests
 * ending in that extension. For Apache/JServ this looks something like
 * this:
 *
 * <pre>
 * ApJServAction .wm /servlets/com.samskivert.velocity.Dispatcher
 * </pre>
 *
 * The request URI then defines the path of the template that will be used
 * to satisfy the request. To understand how code is selected to go along
 * with the request, let's look at an example. Consider the following
 * configuration:
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
 * them into friendly error messages that are inserted into the invocation
 * context with the key <code>"error"</code> for easy display in the
 * resulting web page.
 *
 * <p> The process of mapping exceptions to friendly error messages is
 * done using the {@link ExceptionMap} class. Consult its documentation
 * for an explanation of how it works.
 *
 * @see Logic
 * @see ExceptionMap
 */
public class DispatcherServlet extends VelocityServlet
{
    /**
     * We load our velocity properties from the classpath rather than from
     * a file.
     */
    protected Properties loadConfiguration (ServletConfig config)
        throws IOException
    {
        String propsPath = config.getInitParameter(INIT_PROPS_KEY);
        // config util loads properties files from the classpath
        return ConfigUtil.loadProperties(propsPath);
    }

    /**
     * Initialize ourselves and our application.
     */
    public void init (ServletConfig config)
        throws ServletException
    {
        super.init(config);

        // Log.info("Initializing dispatcher servlet.");

        // load up our application configuration
        try {
            String appcl = config.getInitParameter(APP_CLASS_PROPS_KEY);
            if (appcl == null) {
                _app = new Application();
            } else {
                Class appclass = Class.forName(appcl);
                _app = (Application)appclass.newInstance();
            }
                
            // now initialize the applicaiton
            String logicPkg = config.getInitParameter(LOGIC_PKG_PROPS_KEY);
            if (StringUtil.blank(logicPkg)) {
                logicPkg = "";
            }
            _app.init(getServletContext());
            _app.postInit(getServletContext(), logicPkg);

        } catch (Throwable t) {
            Log.warning("Error instantiating application.");
            Log.logStackTrace(t);
        }
    }

    // documentation inherited
    protected void initVelocity (ServletConfig config)
         throws ServletException
    {
        // stick the servlet context into the Velocity application context
        Velocity.setApplicationContext(getServletContext());
        super.initVelocity(config);
    }

    /**
     * Clean up after ourselves and our application.
     */
    public void destroy ()
    {
        super.destroy();
	// shutdown our application
        _app.shutdown();
    }

    /**
     * Loads up the template appropriate for this request, locates and
     * invokes any associated logic class and finally returns the prepared
     * template which will be merged with the prepared context.
     */
    public Template handleRequest (HttpServletRequest req,
                                   HttpServletResponse rsp,
                                   Context ctx) throws Exception
    {
        InvocationContext ictx = (InvocationContext)ctx;
        String errmsg = null;

	// first we select the template
	Template tmpl = selectTemplate(ictx);

	// assume an HTML response unless otherwise massaged by the logic
	rsp.setContentType("text/html");

	try {
            // insert the application into the context in case the
            // logic or a tool wishes to make use of it
            ictx.put(APPLICATION_KEY, _app);

            // if the application provides a message manager, we want
            // to put a message resolver in the context as well
            MessageManager msgmgr = _app.getMessageManager();
            if (msgmgr != null) {
                MessageResolver mrslv = new MessageResolver(msgmgr);
                ictx.put(MSGRESOLVER_KEY, mrslv);
            }

            // create a form tool for use by the template
            FormTool form = new FormTool(req);
            ictx.put(FORMTOOL_KEY, form);

            // resolve the appropriate logic class for this URI and
            // execute it if it exists
            String path = req.getServletPath();
            Logic logic = resolveLogic(path);
            if (logic != null) {
                logic.invoke(_app, ictx);
            }

	} catch (RedirectException re) {
            ictx.getResponse().sendRedirect(re.getRedirectURL());
            return null;

	} catch (FriendlyException fe) {
            // grab the error message, we'll deal with it shortly
            errmsg = fe.getMessage();

	} catch (Exception e) {
            errmsg = ExceptionMap.getMessage(e);
	    Log.logStackTrace(e);
	}

        // if we have an error message, insert it into the template
        if (errmsg != null) {
            // try using the application to localize the error message
            // before we insert it
            MessageManager msgmgr = _app.getMessageManager();
            if (msgmgr != null) {
                errmsg = msgmgr.getMessage(req, errmsg);
            }
	    ictx.put(ERROR_KEY, errmsg);
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
    public static Application getApplication (InvocationContext context)
    {
        return (Application)context.get(APPLICATION_KEY);
    }

    /**
     * We override this to create a context of our own devising.
     */
    protected Context createContext (HttpServletRequest req,
                                     HttpServletResponse rsp)
    {
        return new InvocationContext(req, rsp);
    }

    /**
     * This method is called to select the appropriate template for this
     * request. The default implementation simply loads the template using
     * Velocity's default template loading services based on the URI
     * provided in the request.
     *
     * @param ctx The context of this request.
     *
     * @return The template to be used in generating the response.
     */
    protected Template selectTemplate (InvocationContext ctx)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        String path = ctx.getRequest().getServletPath();
	// Log.info("Loading template [path=" + path + "].");
        return getTemplate(path);
    }

    /**
     * This method is called to select the appropriate logic for this
     * request URI.
     *
     * @return The logic to be used in generating the response or null if
     * no logic could be matched.
     */
    protected Logic resolveLogic (String path)
    {
	// look for a cached logic instance
	String lclass = _app.generateClass(path);
	Logic logic = (Logic)_logic.get(lclass);

	if (logic == null) {
	    try {
		Class pcl = Class.forName(lclass);
		logic = (Logic)pcl.newInstance();

	    } catch (ClassNotFoundException cnfe) {
                // nothing interesting to report

	    } catch (Throwable t) {
		Log.warning("Unable to instantiate logic for application " +
                            "[path=" + path + ", lclass=" + lclass +
                            ", error=" + t + "].");
	    }

            // if something failed, use a dummy in it's place so that we
            // don't sit around all day freaking out about our inability
            // to instantiate the proper logic class
            if (logic == null) {
		logic = new DummyLogic();
            }

            // cache the resolved logic instance
	    _logic.put(lclass, logic);
	}

	return logic;
    }

    /** The application being served by this dispatcher servlet. */
    protected Application _app;

    /** A table of resolved logic instances. */
    protected HashMap _logic = new HashMap();

    /** This is the key used in the context for error messages. */
    protected static final String ERROR_KEY = "error";

    /**
     * This is the key used to store a reference back to the dispatcher
     * servlet in our invocation context.
     */
    protected static final String APPLICATION_KEY = "%_app_%";

    /**
     * This is the key used to store the message resolver in the context.
     */
    protected static final String MSGRESOLVER_KEY = "i18n";

    /**
     * This is the key used to store the form tool in the context.
     */
    protected static final String FORMTOOL_KEY = "form";

    /** The servlet parameter key specifying the application class. */
    protected static final String APP_CLASS_PROPS_KEY = "app_class";

    /** The servlet parameter key specifying the base logic package. */
    protected static final String LOGIC_PKG_PROPS_KEY = "logic_package";
}
