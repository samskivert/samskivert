//
// $Id: Application.java,v 1.9 2004/06/01 08:34:24 mdb Exp $
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.velocity.app.Velocity;

import com.samskivert.Log;
import com.samskivert.servlet.IndiscriminateSiteIdentifier;
import com.samskivert.servlet.MessageManager;
import com.samskivert.servlet.SiteIdentifier;
import com.samskivert.servlet.SiteResourceLoader;
import com.samskivert.util.StringUtil;

/**
 * The servlet API defines the concept of a web application and associates
 * certain attributes with it like document root and so on. This
 * application class extends that concept by providing a base class that
 * represents the web application. The application class is responsible
 * for initializing services that will be used by the application's logic
 * objects as well as cleaning them up when the application is shut down.
 */
public class Application
{
    /**
     * An initialized application automatically registers itself as a
     * Velocity application attribute so that it can be retrieved by
     * Velocity plugins using
     * <code>getApplicationAttribute(VELOCITY_ATTR_KEY)</code>.
     */
    public static final String VELOCITY_ATTR_KEY = "!application!";

    /**
     * Performs initializations common to all applications. Applications
     * should override {@link #willInit} to perform initializations that
     * need to take place before the common initialization (which includes
     * the creation of the site identifier and message manager) and should
     * override {@link #didInit} to perform initializations that need to
     * take place after the common initialization (like passing the
     * application to entities that might turn around and request a
     * reference to our site identifier).
     *
     * @param config the servlet config from which the application will
     * load configuration information.
     * @param context the servlet context in which this application is
     * operating.
     * @param logicPkg the base package for all of the logic
     * implementations for this application.
     */
    public void init (ServletConfig config, ServletContext context,
                      String logicPkg)
    {
        // keep this around for later
        _context = context;

        // stick ourselves into an application attribute so that we can be
        // accessed by Velocity plugins
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

        // create a site resource loader if the user set up the
        // site-specific jar file path
        String siteJarPath = getInitParameter(config, SITE_JAR_PATH_KEY);
        if (!StringUtil.blank(siteJarPath)) {
            _siteLoader = new SiteResourceLoader(_siteIdent, siteJarPath);
        }

        // instantiate our message manager if the application wants one
        String bundlePath = getInitParameter(config, MESSAGE_BUNDLE_PATH_KEY);
        if (!StringUtil.blank(bundlePath)) {
            _msgmgr = new MessageManager(bundlePath);
        }

        // if we have a site-specific resource loader, configure the
        // message manager with it, so that it can load site-specific
        // message resources
        if (_msgmgr != null && _siteLoader != null) {
            String siteBundlePath = getInitParameter(
                config, SITE_MESSAGE_BUNDLE_PATH_KEY);
            if (!StringUtil.blank(siteBundlePath)) {
                _msgmgr.activateSiteSpecificMessages(
                    siteBundlePath, _siteLoader, _siteIdent);

            } else {
                Log.info("No '" + SITE_MESSAGE_BUNDLE_PATH_KEY + "' " +
                         "specified in servlet configuration. This is " +
                         "required to allow the message manager to load " +
                         "site-specific translation resources.");
            }
        }

        // let the derived application do post-init stuff
        didInit(config);
    }

    /**
     * Looks up an initialization parameter for this application. The
     * default implementation retrieves the value from the servlet config,
     * but derived classes may wish to get certain parameters from some
     * other configuration source.
     */
    protected String getInitParameter (ServletConfig config, String key)
    {
        return config.getInitParameter(key);
    }

    /**
     * This should be overridden by the application implementation to
     * invoke any necessary pre-initialization code. They should be sure
     * to call <code>super.willInit()</code>.
     */
    protected void willInit (ServletConfig config)
    {
    }

    /**
     * This should be overridden by the application implementation to
     * invoke any necessary post-initialization code. They should be sure
     * to call <code>super.didInit()</code>.
     */
    protected void didInit (ServletConfig config)
    {
    }

    /**
     * This should be overridden by the application implementation to
     * perform any necessary cleanup.
     */
    public void shutdown ()
    {
    }

    /**
     * Returns a reference to the servlet context in which this
     * application is operating.
     */
    public ServletContext getServletContext ()
    {
        return _context;
    }

    /**
     * Returns the message manager in effect for this application, if one
     * is in effect.
     */
    public MessageManager getMessageManager ()
    {
        return _msgmgr;
    }

    /**
     * Returns the site identifier in effect for figuring out which site
     * through which a user is making a request.
     */
    public SiteIdentifier getSiteIdentifier ()
    {
        return _siteIdent;
    }

    /**
     * Returns a reference to the loader used to obtain site-specific
     * resources. This is only valid if the user specified the
     * site-specific jar file path in the servlet configuration.
     *
     * @see #SITE_JAR_PATH_KEY
     */
    public SiteResourceLoader getSiteResourceLoader ()
    {
        return _siteLoader;
    }

    /**
     * Called to instantiate the site identifier that we'd like to use in
     * this application. This will be an instance of {@link
     * IndiscriminateSiteIdentifier} unless the derived application class
     * overrides this method and creates something more to its liking.
     * This will be called after the application's {@link #init} method
     * has been called.
     */
    protected SiteIdentifier createSiteIdentifier (ServletContext ctx)
    {
        return new IndiscriminateSiteIdentifier();
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
    public final String translate (InvocationContext ctx, String msg,
                                   Object arg)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg,
                                  new Object[]{ arg });
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (InvocationContext ctx, String msg,
                                   Object arg1, Object arg2)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg,
                                  new Object[]{ arg1, arg2 });
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (InvocationContext ctx, String msg,
                                   Object arg1, Object arg2, Object arg3)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg,
                                  new Object[]{ arg1, arg2, arg3 });
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (InvocationContext ctx, String msg,
                                   Object[] args)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg, args);
    }

    /**
     * Given the servlet path (the part of the URI after the context path)
     * this generates the classname of the logic class that should handle
     * the request.
     */
    protected String generateClass (String path)
    {
        // remove the trailing file extension
        int ldidx = path.lastIndexOf(".");
        if (ldidx != -1) {
            path = path.substring(0, ldidx);
        }
        // convert slashes to dots
        path = StringUtil.replace(path, "/", ".");
        // prepend the base logic package and we're all set
        return _logicPkg + path;
    }

    /** A reference to the servlet context in which this application is
     * operating. */
    protected ServletContext _context;

    /** The prefix that we use to generate fully qualified logic class
     * names. */
    protected String _logicPkg;

    /** A reference to our message manager or null if we have none. */
    protected MessageManager _msgmgr;

    /** A reference to our site identifier. */
    protected SiteIdentifier _siteIdent;

    /** Provides access to site-specific resources. */
    protected SiteResourceLoader _siteLoader;

    /** The servlet parameter key specifying the path to the application's
     * translated message resources. */
    protected static final String MESSAGE_BUNDLE_PATH_KEY = "messages_path";

    /** The servlet parameter key specifying the path to the site-specific
     * jar files. */
    protected static final String SITE_JAR_PATH_KEY = "site_jar_path";

    /** The servlet parameter key specifying the path to the site-specific
     * translated message resources. */
    protected static final String SITE_MESSAGE_BUNDLE_PATH_KEY =
        "site_messages_path";
}
