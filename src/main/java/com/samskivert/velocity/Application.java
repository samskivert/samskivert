//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.app.Velocity;

import com.samskivert.servlet.HttpErrorException;
import com.samskivert.servlet.MessageManager;
import com.samskivert.servlet.RedirectException;
import com.samskivert.servlet.SiteIdentifier;
import com.samskivert.servlet.SiteIdentifiers;
import com.samskivert.servlet.SiteResourceLoader;
import com.samskivert.servlet.util.ExceptionMap;
import com.samskivert.servlet.util.FriendlyException;
import com.samskivert.servlet.util.RequestUtils;
import com.samskivert.util.StringUtil;

import static com.samskivert.servlet.Log.log;

/**
 * The servlet API defines the concept of a web application and associates certain attributes with
 * it like document root and so on. This application class extends that concept by providing a base
 * class that represents the web application. The application class is responsible for initializing
 * services that will be used by the application's logic objects as well as cleaning them up when
 * the application is shut down.
 *
 * <p><b>Error handling</b><br>
 * The application provides a common error handling mechanism. The design is to catch any
 * exceptions thrown by the logic and to convert them into friendly error messages that are
 * inserted into the invocation context with the key <code>"error"</code> for easy display in the
 * resulting web page.
 *
 * <p> The default process of mapping exceptions to friendly error messages is done using the
 * {@link ExceptionMap} class. This can be replaced by overriding {@link #handleException}.
 */
public class Application
{
    /**
     * An initialized application automatically registers itself as a Velocity application
     * attribute so that it can be retrieved by Velocity plugins using
     * <code>getApplicationAttribute(VELOCITY_ATTR_KEY)</code>.
     */
    public static final String VELOCITY_ATTR_KEY = "!application!";

    /**
     * Performs initializations common to all applications. Applications should override {@link
     * #willInit} to perform initializations that need to take place before the common
     * initialization (which includes the creation of the site identifier and message manager) and
     * should override {@link #didInit} to perform initializations that need to take place after
     * the common initialization (like passing the application to entities that might turn around
     * and request a reference to our site identifier).
     *
     * @param config the servlet config from which the application will load configuration
     * information.
     * @param context the servlet context in which this application is operating.
     * @param logicPkg the base package for all of the logic implementations for this application.
     */
    public void init (ServletConfig config, ServletContext context, String logicPkg)
    {
        // keep this around for later
        _context = context;

        // stick ourselves into an application attribute so that we can be accessed by Velocity
        // plugins
        Velocity.setApplicationAttribute(VELOCITY_ATTR_KEY, this);

        // let the derived application do pre-init stuff
        willInit(config);

        // remove any trailing dot
        if (logicPkg.endsWith(".")) {
            _logicPkg = logicPkg.substring(0, logicPkg.length()-1);
        } else {
            _logicPkg = logicPkg;
        }

        // create our site identifier
        _siteIdent = createSiteIdentifier(_context);

        // create a site resource loader if the user set up the site-specific jar file path
        String siteJarPath = getInitParameter(config, SITE_JAR_PATH_KEY);
        if (!StringUtil.isBlank(siteJarPath)) {
            _siteLoader = new SiteResourceLoader(_siteIdent, siteJarPath);
        }

        // instantiate our message manager if the application wants one
        String bundlePath = getInitParameter(config, MESSAGE_BUNDLE_PATH_KEY);
        if (!StringUtil.isBlank(bundlePath)) {
            _msgmgr = createMessageManager(bundlePath);
        }

        // if we have a site-specific resource loader, configure the message manager with it, so
        // that it can load site-specific message resources
        if (_msgmgr != null && _siteLoader != null) {
            String siteBundlePath = getInitParameter(config, SITE_MESSAGE_BUNDLE_PATH_KEY);
            if (!StringUtil.isBlank(siteBundlePath)) {
                _msgmgr.activateSiteSpecificMessages(siteBundlePath, _siteLoader);

            } else {
                log.info("No '" + SITE_MESSAGE_BUNDLE_PATH_KEY + "' specified in servlet " +
                         "configuration. This is required to allow the message manager to load " +
                         "site-specific translation resources.");
            }
        }

        // let the derived application do post-init stuff
        didInit(config);
    }

    /**
     * Called prior to initializing Velocity to allow the application to specify custom
     * configuration properties.
     */
    protected void configureVelocity (ServletConfig config, Properties props)
    {
    }

    /**
     * Looks up an initialization parameter for this application. The default implementation
     * retrieves the value from the servlet config, but derived classes may wish to get certain
     * parameters from some other configuration source.
     */
    protected String getInitParameter (ServletConfig config, String key)
    {
        return config.getInitParameter(key);
    }

    /**
     * Creates the message manager to be used for this application.
     */
    protected MessageManager createMessageManager (String bundlePath)
    {
        return new MessageManager(bundlePath, Locale.getDefault(), _siteIdent);
    }

    /**
     * This should be overridden by the application implementation to invoke any necessary
     * pre-initialization code. They should be sure to call <code>super.willInit()</code>.
     */
    protected void willInit (ServletConfig config)
    {
    }

    /**
     * This should be overridden by the application implementation to invoke any necessary
     * post-initialization code. They should be sure to call <code>super.didInit()</code>.
     */
    protected void didInit (ServletConfig config)
    {
    }

    /**
     * Allows derived aplication classes to prepare an invocation context prior to the logic class
     * being invoked. They may wish to add standard tools to the context or do any other
     * request-invariant preparation.
     */
    protected void prepareContext (InvocationContext ctx)
    {
    }

    /**
     * Allows derived application classes to check access in a single location prior to resolving
     * and dispatching a logic class, if they desire access control at this level. Alternatively,
     * access control can be performed by the logic instance.
     */
    protected void checkAccess (InvocationContext ctx)
        throws RedirectException, HttpErrorException
    {
    }

    /**
     * If an exception propagates up from {@link Logic#invoke}, the application is given the chance
     * to convert a low-level exception into a {@link FriendlyException} or a {@link
     * RedirectException} which will be handled in the normal way.
     */
    protected Exception translateException (Exception error)
    {
        return error;
    }

    /**
     * If a generic exception propagates up from {@link Logic#invoke} and is not otherwise
     * converted into a friendly or redirect exception, the application will be required to provide
     * a generic error message to be inserted into the context and should take this opportunity to
     * log the exception.
     *
     * <p><em>Note:</em> the string returned by this method will be translated using the
     * application's message manager before being inserted into the Velocity context.
     */
    protected String handleException (HttpServletRequest req, Logic logic, Exception error)
    {
        log.warning(logic + " failed on: " + RequestUtils.reconstructURL(req), error);
        return ExceptionMap.getMessage(error);
    }

    /**
     * This should be overridden by the application implementation to perform any necessary
     * cleanup.
     */
    public void shutdown ()
    {
    }

    /**
     * Returns a reference to the servlet context in which this application is operating.
     */
    public ServletContext getServletContext ()
    {
        return _context;
    }

    /**
     * Returns the message manager in effect for this application, if one is in effect.
     */
    public MessageManager getMessageManager ()
    {
        return _msgmgr;
    }

    /**
     * Returns the site identifier in effect for figuring out which site through which a user is
     * making a request.
     */
    public SiteIdentifier getSiteIdentifier ()
    {
        return _siteIdent;
    }

    /**
     * Returns a reference to the loader used to obtain site-specific resources. This is only valid
     * if the user specified the site-specific jar file path in the servlet configuration.
     *
     * @see #SITE_JAR_PATH_KEY
     */
    public SiteResourceLoader getSiteResourceLoader ()
    {
        return _siteLoader;
    }

    /**
     * Called to instantiate the site identifier that we'd like to use in this application. This
     * will be {@link SiteIdentifiers#DEFAULT}} unless the derived application class overrides this
     * method and creates something more to its liking. This will be called after the application's
     * {@link #init} method has been called.
     */
    protected SiteIdentifier createSiteIdentifier (ServletContext ctx)
    {
        return SiteIdentifiers.DEFAULT;
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (InvocationContext ctx, String msg)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg);
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (InvocationContext ctx, String msg, Object... args)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg, args);
    }

    /**
     * Given the servlet path (the part of the URI after the context path) this generates the
     * classname of the logic class that should handle the request.
     */
    protected String generateClass (String path)
    {
        // remove the trailing file extension
        int ldidx = path.lastIndexOf(".");
        if (ldidx != -1) {
            path = path.substring(0, ldidx);
        }
        // convert slashes to dots
        path = path.replace('/', '.');
        // prepend the base logic package and we're all set
        return _logicPkg + path;
    }

    /** A reference to the servlet context in which this application is operating. */
    protected ServletContext _context;

    /** The prefix that we use to generate fully qualified logic class names. */
    protected String _logicPkg;

    /** A reference to our message manager or null if we have none. */
    protected MessageManager _msgmgr;

    /** A reference to our site identifier. */
    protected SiteIdentifier _siteIdent;

    /** Provides access to site-specific resources. */
    protected SiteResourceLoader _siteLoader;

    /** The servlet parameter key specifying the path to the application's translated message
     * resources. */
    protected static final String MESSAGE_BUNDLE_PATH_KEY = "messages_path";

    /** The servlet parameter key specifying the path to the site-specific jar files. */
    protected static final String SITE_JAR_PATH_KEY = "site_jar_path";

    /** The servlet parameter key specifying the path to the site-specific translated message
     * resources. */
    protected static final String SITE_MESSAGE_BUNDLE_PATH_KEY = "site_messages_path";
}
