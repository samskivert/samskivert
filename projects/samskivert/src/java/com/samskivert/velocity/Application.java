//
// $Id: Application.java,v 1.5 2001/11/06 04:49:32 mdb Exp $
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

import javax.servlet.ServletContext;

import com.samskivert.servlet.IndiscriminateSiteIdentifier;
import com.samskivert.servlet.MessageManager;
import com.samskivert.servlet.SiteIdentifier;
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
     * Performs initializations common to all applications. Applications
     * should override {@link #willInit} to perform initializations that
     * need to take place before the common initialization (which includes
     * the creation of the site identifier and message manager) and should
     * override {@link #didInit} to perform initializations that need to
     * take place after the common initialization (like passing the
     * application to entities that might turn around and request a
     * reference to our site identifier).
     *
     * @param context the servlet context in which this application is
     * operating.
     * @param logicPkg the base package for all of the logic
     * implementations for this application.
     */
    public void init (ServletContext context, String logicPkg)
    {
        // keep this around for later
        _context = context;

        // let the derived application do pre-init stuff
        willInit();

        // remove any trailing dot
        if (logicPkg.endsWith(".")) {
            _logicPkg = logicPkg.substring(0, logicPkg.length()-1);
        } else {
            _logicPkg = logicPkg;
        }

        // instantiate our message manager if the application wants one
        String bpath = getMessageBundlePath();
        if (bpath != null) {
            _msgmgr = new MessageManager(bpath);
        }

        // create our site identifier
        _siteIdent = createSiteIdentifier(_context);

        // let the derived application do post-init stuff
        didInit();
    }

    /**
     * This should be overridden by the application implementation to
     * invoke any necessary pre-initialization code. They should be sure
     * to call <code>super.willInit()</code>.
     */
    protected void willInit ()
    {
    }

    /**
     * This should be overridden by the application implementation to
     * invoke any necessary post-initialization code. They should be sure
     * to call <code>super.didInit()</code>.
     */
    protected void didInit ()
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
     * If an application wishes to make use of the translation facilities
     * provided by the message manager, it need only provide the path to
     * its message resource bundle via this member function. Using a
     * message manager allows framework components like the
     * <code>MsgTool</code> to make use of the application's message
     * bundles.
     */
    protected String getMessageBundlePath ()
    {
        return null;
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
}
