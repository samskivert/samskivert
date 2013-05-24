//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.util.SimplePool;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.MethodExceptionEventHandler;

import com.samskivert.servlet.HttpErrorException;
import com.samskivert.servlet.MessageManager;
import com.samskivert.servlet.RedirectException;
import com.samskivert.servlet.SiteIdentifier;
import com.samskivert.servlet.SiteResourceLoader;
import com.samskivert.servlet.util.FriendlyException;

import com.samskivert.util.ConfigUtil;
import com.samskivert.util.StringUtil;

import static com.samskivert.servlet.Log.log;

/**
 * The dispatcher servlet builds upon Velocity's architecture. It does so in the following ways:
 *
 * <ul> <li> It defines the notion of a logic object which populates the context with data to be
 * used to satisfy a particular request. The logic is not a servlet and is therefore limited in
 * what it can do while populating data. Experience dictates that ultimate flexibility leads to bad
 * design decisions and that this is a place where that sort of thing can be comfortably nipped in
 * the bud. <br><br>
 *
 * <li> It allows template files to be referenced directly in the URL while maintaining the ability
 * to choose a cobranded template based on information in the request. The URI is mapped to a
 * servlet based on some simple mapping rules. This provides template designers with a clearer
 * understanding of the structure of a web application as well as with an easy way to test their
 * templates in the absence of an associated servlet. <br><br>
 *
 * <li> It provides a common error handling paradigm that simplifies the task of authoring web
 * applications.
 * </ul>
 *
 * <p><b>URI to servlet mapping</b><br> The mapping process allows the Velocity framework to be
 * invoked for all requests ending in a particular file extension (usually <code>.wm</code>). It is
 * necessary to instruct your servlet engine of choice to invoke the <code>DispatcherServlet</code>
 * for all requests ending in that extension. For Apache/JServ this looks something like this:
 *
 * <pre>
 * ApJServAction .wm /servlets/com.samskivert.velocity.Dispatcher
 * </pre>
 *
 * The request URI then defines the path of the template that will be used to satisfy the
 * request. To understand how code is selected to go along with the request, let's look at an
 * example. Consider the following configuration:
 *
 * <pre>
 * applications=whowhere
 * whowhere.base_uri=/whowhere
 * whowhere.base_pkg=whowhere.logic
 * </pre>
 *
 * This defines an application identified as <code>whowhere</code>. An application is defined by
 * three parameters, the application identifier, the <code>base_uri</code>, and the
 * <code>base_pkg</code>. The <code>base_uri</code> defines the prefix shared by all pages served
 * by the application and which serves to identify which application to invoke when processing a
 * request. The <code>base_pkg</code> is used to construct the logic classname based on the URI and
 * the <code>base_uri</code> parameter.
 *
 * <p> Now let's look at a sample request to determine how the logic classname is
 * resolved. Consider the following request URI:
 *
 * <pre>
 * /whowhere/view/trips.wm
 * </pre>
 *
 * It begins with <code>/whowhere</code> which tells the dispatcher that it's part of the
 * <code>whowhere</code> application. That application's <code>base_uri</code> is then stripped
 * from the URI leaving <code>/view/trips.wm</code>. The slashes are converted into periods to map
 * directories to packages, giving us <code>view.trips.wm</code>.  Finally, the
 * <code>base_pkg</code> is prepended and the trailing <code>.wm</code> extension removed.
 *
 * <p> Thus the class invoked to populate the context for this request is
 * <code>whowhere.servlets.view.trips</code> (note that the classname <em>is</em> lowercase which
 * is an intentional choice in resolving conflicting recommendations that classnames should always
 * start with a capital letter and URLs should always be lowercase).
 *
 * <p> The template used to generate the result is loaded based on the full URI, essentially with a
 * call to <code>getTemplate("/whowhere/view/trips.wm")</code> in this example.  This is the place
 * where more sophisticated cobranding support could be inserted in the future (ie. if I ever want
 * to use this to develop a cobranded web site).
 *
 * @see Logic
 */
public class DispatcherServlet extends HttpServlet
    implements MethodExceptionEventHandler
{
    /** The HTTP content type context key. */
    public static final String CONTENT_TYPE = "default.contentType";

    @Override
    public void init (ServletConfig config)
        throws ServletException
    {
        super.init(config);

        // create and initialize our application
        try {
            _app = createApp(config);
            _app.init(config, getServletContext(), getLogicPackage(config));
        } catch (Throwable t) {
            throw new ServletException("Error instantiating Application: " + t, t);
        }

        try {
            Velocity.init(loadConfiguration(config));
        } catch (Exception e) {
            throw new ServletException("Error initializing Velocity: " + e, e);
        }

        _defaultContentType = RuntimeSingleton.getString(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);

        // determine the character set we'll use
        _charset = config.getInitParameter(CHARSET_KEY);
        if (_charset == null) {
            _charset = "UTF-8";
        }
    }

    /**
     * Handles HTTP <code>GET</code> requests by calling {@link #doRequest}.
     */
    @Override
    public void doGet (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doRequest(request, response);
    }

    /**
     * Handles HTTP <code>POST</code> requests by calling {@link #doRequest}.
     */
    @Override
    public void doPost (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doRequest(request, response);
    }

    /**
     * Clean up after ourselves and our application.
     */
    @Override
    public void destroy ()
    {
        super.destroy();
        // shutdown our application
        _app.shutdown();
    }

    protected Application createApp (ServletConfig config) throws Exception {
        String appcl = config.getInitParameter(APP_CLASS_KEY);
        if (StringUtil.isBlank(appcl)) {
            return new Application();
        } else {
            Class<?> appclass = Class.forName(appcl);
            return (Application)appclass.newInstance();
        }
    }

    protected String getLogicPackage (ServletConfig config) {
        String logicPkg = config.getInitParameter(LOGIC_PKG_KEY);
        return StringUtil.isBlank(logicPkg) ? "" : logicPkg;
    }

    protected Properties loadVelocityProps (ServletConfig config) throws IOException {
        String propsPath = config.getInitParameter(INIT_PROPS_KEY);
        if (propsPath == null) {
            throw new IOException(INIT_PROPS_KEY + " must point to the velocity properties file " +
                                  "in the servlet configuration.");
        }
        // config util loads properties files from the classpath
        Properties props = ConfigUtil.loadProperties(propsPath);
        if (props == null) {
            throw new IOException("Unable to load velocity properties " +
                                  "from file '" + INIT_PROPS_KEY + "'.");
        }
        return props;
    }

    /**
     * We load our velocity properties from the classpath rather than from a file.
     */
    protected Properties loadConfiguration (ServletConfig config)
        throws IOException
    {
        Properties props = loadVelocityProps(config);

        // if we failed to create our application for whatever reason; bail
        if (_app == null) {
            return props;
        }

        // let the application set up velocity properties
        _app.configureVelocity(config, props);

        // if no file resource loader path has been set and a site-specific jar file path was
        // provided, wire up our site resource manager
        configureResourceManager(config, props);

        // wire up our #import directive
        props.setProperty("userdirective", ImportDirective.class.getName());

        // configure the servlet context logger
        props.put(RuntimeSingleton.RUNTIME_LOG_LOGSYSTEM_CLASS,
                  ServletContextLogger.class.getName());

        // now return our augmented properties
        return props;
    }

    protected void configureResourceManager (ServletConfig config, Properties props) {
        if (props.getProperty("file.resource.loader.path") == null) {
            SiteResourceLoader siteLoader = _app.getSiteResourceLoader();
            if (siteLoader != null) {
                log.info("Velocity loading templates from site loader.");
                props.setProperty(RuntimeSingleton.RESOURCE_MANAGER_CLASS,
                                  SiteResourceManager.class.getName());
                _usingSiteLoading = true;
            } else {
                // otherwise use a servlet context resource loader
                log.info("Velocity loading templates from servlet context.");
                props.setProperty(RuntimeSingleton.RESOURCE_MANAGER_CLASS,
                                  ServletContextResourceManager.class.getName());
            }
        }
    }

    /**
     * Loads up the template appropriate for this request, locates and invokes any associated logic
     * class and finally returns the prepared template which will be merged with the prepared
     * context.
     */
    public Template handleRequest (HttpServletRequest req, HttpServletResponse rsp, Context ctx)
        throws Exception
    {
        InvocationContext ictx = (InvocationContext)ctx;
        Logic logic = null;

        // listen for exceptions so that we can report them
        EventCartridge ec = ictx.getEventCartridge();
        if (ec == null) {
            ec = new EventCartridge();
            ec.attachToContext(ictx);
            ec.addEventHandler(this);
        }

        // if our application failed to initialize, fail with a 500 response
        if (_app == null) {
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

        // obtain the siteid for this request and stuff that into the context
        int siteId = SiteIdentifier.DEFAULT_SITE_ID;
        SiteIdentifier ident = _app.getSiteIdentifier();
        if (ident != null) {
            siteId = ident.identifySite(req);
        }
        if (_usingSiteLoading) {
            ctx.put("__siteid__", Integer.valueOf(siteId));
        }

        // put the context path in the context as well to make it easier to
        // construct full paths
        ctx.put("context_path", req.getContextPath());

        // then select the template
        Template tmpl = null;
        try {
            tmpl = selectTemplate(siteId, ictx);
        } catch (ResourceNotFoundException rnfe) {
            // send up a 404.  For some annoying reason, Jetty tells Apache
            // that all is okay (200) when sending its own custom error pages,
            // forcing us to use Jetty's custom error page handling code rather
            // than passing it up the chain to be dealt with appropriately.
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // assume the request is in the default character set unless it has
        // actually been sensibly supplied by the browser
        if (req.getCharacterEncoding() == null) {
            req.setCharacterEncoding(_charset);
        }

        // assume an HTML response in the default character set unless
        // otherwise massaged by the logic
        rsp.setContentType("text/html; charset=" + _charset);

        Exception error = null;
        try {
            // insert the application into the context in case the logic or a
            // tool wishes to make use of it
            ictx.put(APPLICATION_KEY, _app);

            // if the application provides a message manager, we want create a
            // translation tool and stuff that into the context
            MessageManager msgmgr = _app.getMessageManager();
            if (msgmgr != null) {
                I18nTool i18n = new I18nTool(req, msgmgr);
                ictx.put(I18NTOOL_KEY, i18n);
            }

            // create a form tool for use by the template
            FormTool form = new FormTool(req);
            ictx.put(FORMTOOL_KEY, form);

            // create a new string tool for use by the template
            StringTool string = new StringTool();
            ictx.put(STRINGTOOL_KEY, string);

            // create a new data tool for use by the tempate
            DataTool datatool = new DataTool();
            ictx.put(DATATOOL_KEY, datatool);

            // create a curreny tool set up to use the correct locale
            CurrencyTool ctool = new CurrencyTool(req.getLocale());
            ictx.put(CURRENCYTOOL_KEY, ctool);

            // allow the application to prepare the context
            _app.prepareContext(ictx);

            // allow the application to do global access control
            _app.checkAccess(ictx);

            // resolve the appropriate logic class for this URI and execute it
            // if it exists
            String path = req.getServletPath();
            logic = resolveLogic(path);
            if (logic != null) {
                logic.invoke(_app, ictx);
            }

        } catch (Exception e) {
            error = e;
        }

        // if an error occurred processing the template, allow the application
        // to convert it to something more appropriate and then handle it
        String errmsg = null;
        try {
            if (error != null) {
                throw _app.translateException(error);
            }

        } catch (RedirectException re) {
            rsp.sendRedirect(re.getRedirectURL());
            return null;

        } catch (HttpErrorException hee) {
            String msg = hee.getErrorMessage();
            if (msg != null) {
                rsp.sendError(hee.getErrorCode(), msg);
            } else {
                rsp.sendError(hee.getErrorCode());
            }
            return null;

        } catch (FriendlyException fe) {
            // grab the error message, we'll deal with it shortly
            errmsg = fe.getMessage();

        } catch (Exception e) {
            errmsg = _app.handleException(req, logic, e);
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
     * Called when a method throws an exception during template evaluation.
     */
    @SuppressWarnings("rawtypes") // our super class declares a bare Class
    public Object methodException (Class clazz, String method, Exception e)
        throws Exception
    {
        log.warning("Exception", "class", clazz.getName(), "method", method, e);
        return "";
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
     * Handles all requests (by default).
     *
     * @param request HttpServletRequest object containing client request.
     * @param response HttpServletResponse object for the response.
     */
    protected void doRequest (HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
    {
        InvocationContext context = null;
        try {
            context =  new InvocationContext(request, response);
            setContentType(request, response);

            Template template = handleRequest(request, response, context);
            if (template != null) {
                mergeTemplate(template, context);
            }

        } catch (Exception e) {
            log.warning("doRequest failed", "uri", request.getRequestURI(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Sets the content type of the response, defaulting to {@link #_defaultContentType} if not
     * overriden. Delegates to {@link #chooseCharacterEncoding(HttpServletRequest)} to select the
     * appropriate character encoding.
     */
    protected void setContentType (HttpServletRequest request, HttpServletResponse response)
    {
        String contentType = _defaultContentType;
        int index = contentType.lastIndexOf(';') + 1;
        if (index <= 0 || (index < contentType.length() &&
                           contentType.indexOf("charset", index) == -1)) {
            // append the character encoding which we'd like to use
            String encoding = chooseCharacterEncoding(request);
            if (!DEFAULT_OUTPUT_ENCODING.equalsIgnoreCase(encoding)) {
                contentType += "; charset=" + encoding;
            }
        }
        response.setContentType(contentType);
    }

    /**
     * Chooses the output character encoding to be used as the value for the "charset=" portion of
     * the HTTP Content-Type header (and thus returned by
     * <code>response.getCharacterEncoding()</code>). Called by {@link #setContentType} if an
     * encoding isn't already specified by Content-Type.  By default, chooses the value of
     * RuntimeSingleton's <code>output.encoding</code> property.
     */
    protected String chooseCharacterEncoding (HttpServletRequest request)
    {
        return RuntimeSingleton.getString(
            RuntimeConstants.OUTPUT_ENCODING, DEFAULT_OUTPUT_ENCODING);
    }

    /**
     * This method is called to select the appropriate template for this request. The default
     * implementation simply loads the template using Velocity's default template loading services
     * based on the URI provided in the request.
     *
     * @param ctx The context of this request.
     *
     * @return The template to be used in generating the response.
     */
    protected Template selectTemplate (int siteId, InvocationContext ctx)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        String path = ctx.getRequest().getServletPath();
        if (_usingSiteLoading) {
            // if we're using site resource loading, we need to prefix the path with the site
            // identifier
            path = siteId + ":" + path;
        }
        // log.info("Loading template", "path", path);
        return RuntimeSingleton.getTemplate(path);
    }

    /**
     * Merges the template with the context.
     *
     * @param template template object returned by the {@link #handleRequest} method.
     * @param context the context for this request.
     */
    protected void mergeTemplate (Template template, InvocationContext context)
        throws ResourceNotFoundException, ParseErrorException, MethodInvocationException,
               UnsupportedEncodingException, IOException, Exception
    {
        HttpServletResponse response = context.getResponse();
        ServletOutputStream output = response.getOutputStream();
        // ASSUMPTION: response.setContentType() has been called.
        String encoding = response.getCharacterEncoding();

        VelocityWriter vw = null;
        try {
            vw = (VelocityWriter)_writerPool.get();
            if (vw == null) {
                vw = new VelocityWriter(new OutputStreamWriter(output, encoding), 4 * 1024, true);
            } else {
                vw.recycle(new OutputStreamWriter(output, encoding));
            }
            template.merge(context, vw);

        } catch (IOException ioe) {
            // the client probably crashed or aborted the connection ungracefully, so use log.info
            log.info("Failed to write response", "uri", context.getRequest().getRequestURI(),
                     "error", ioe);

        } finally {
            if (vw != null) {
                try {
                    // flush and put back into the pool don't close to allow us to play nicely with
                    // others.
                    vw.flush();
                } catch (IOException e) {
                    // do nothing
                }

                // Clear the VelocityWriter's reference to its internal OutputStreamWriter to allow
                // the latter to be GC'd while vw is pooled.
                vw.recycle(null);
                _writerPool.put(vw);
            }
        }
    }

    /**
     * This method is called to select the appropriate logic for this request URI.
     *
     * @return The logic to be used in generating the response or null if no logic could be
     * matched.
     */
    protected Logic resolveLogic (String path)
    {
        // look for a cached logic instance
        String lclass = _app.generateClass(path);
        Logic logic = _logic.get(lclass);

        if (logic == null) {
            logic = instantiateLogic(path, lclass);

            // if something failed, use a dummy in it's place so that we don't sit around all day
            // freaking out about our inability to instantiate the proper logic class
            if (logic == null) {
                logic = new DummyLogic();
            }

            // cache the resolved logic instance
            _logic.put(lclass, logic);
        }

        return logic;
    }

    /**
     * Instantiates a logic instance with the supplied class name. May return null if no such class
     * exists.
     */
    protected Logic instantiateLogic (String path, String lclass) {
        try {
            Class<?> pcl = Class.forName(lclass);
            return (Logic)pcl.newInstance();
        } catch (ClassNotFoundException cnfe) {
            // nothing interesting to report
        } catch (Throwable t) {
            log.warning("Unable to instantiate logic for application", "path", path,
                        "lclass", lclass, t);
        }
        return null;
    }

    /** The application being served by this dispatcher servlet. */
    protected Application _app;

    /** A table of resolved logic instances. */
    protected HashMap<String,Logic> _logic = new HashMap<String,Logic>();

    /** The character set in which serve our responses. */
    protected String _charset;

    /** Set to true if we're using the {@link SiteResourceLoader}. */
    protected boolean _usingSiteLoading;

    /** Our default content type. */
    protected String _defaultContentType;

    /** A pool of VelocityWriter instances. */
    protected static SimplePool _writerPool = new SimplePool(40);

    /** Describes the location of our properties. */
    protected static final String INIT_PROPS_KEY = "org.apache.velocity.properties";

    /** This is the key used in the context for error messages. */
    protected static final String ERROR_KEY = "error";

    /** This is the key used to store a reference back to the dispatcher servlet in our invocation
     * context. */
    protected static final String APPLICATION_KEY = "%_app_%";

    /** The key used to store the translation tool in the context. */
    protected static final String I18NTOOL_KEY = "i18n";

    /** The key used to store the form tool in the context. */
    protected static final String FORMTOOL_KEY = "form";

    /** The key used to store the string tool in the context. */
    protected static final String STRINGTOOL_KEY = "string";

    /** The key used to store the data tool in the context. */
    protected static final String DATATOOL_KEY = "data";

    /** The key used to store the currency tool in the context. */
    protected static final String CURRENCYTOOL_KEY = "cash";

    /** The servlet parameter key specifying the application class. */
    protected static final String APP_CLASS_KEY = "app_class";

    /** The servlet parameter key specifying the base logic package. */
    protected static final String LOGIC_PKG_KEY = "logic_package";

    /** The servlet parameter key specifying the default character set. */
    protected static final String CHARSET_KEY = "charset";

    /** The default content type for responses. */
    protected static final String DEFAULT_CONTENT_TYPE = "text/html";

    /** The default encoding for the output stream. */
    protected static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";
}
