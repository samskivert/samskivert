//
// $Id: Application.java,v 1.4 2001/08/11 22:43:29 mdb Exp $
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

package com.samskivert.webmacro;

import javax.servlet.ServletContext;
import org.webmacro.servlet.WebContext;

import com.samskivert.servlet.MessageManager;
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
     * This should be overridden by the application implementation to
     * perform any necessary initialization.
     */
    public void init (ServletContext context)
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
     * Returns the message manager in effect for this application, if one
     * is in effect.
     */
    public MessageManager getMessageManager ()
    {
        return _msgmgr;
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
     * A convenience function for translating messages.
     */
    public final String translate (WebContext ctx, String msg)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg);
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (WebContext ctx, String msg, Object arg)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg,
                                  new Object[]{ arg });
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (WebContext ctx, String msg,
                                   Object arg1, Object arg2)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg,
                                  new Object[]{ arg1, arg2 });
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (WebContext ctx, String msg,
                                   Object arg1, Object arg2, Object arg3)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg,
                                  new Object[]{ arg1, arg2, arg3 });
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (WebContext ctx, String msg,
                                   Object[] args)
    {
        return _msgmgr.getMessage(ctx.getRequest(), msg, args);
    }

    /**
     * Performs initializations common to all applications. We could put
     * this in init() and require application derived classes to call
     * super.init() but people would forget to do so and be confused.
     *
     * <p> The default application implementation takes the base URI and
     * base package as defined in the application declaration in the
     * servlet configuration and uses those to determine whether or not a
     * given URI maps to this application.
     */
    public void preInit (String baseURI, String basePkg)
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

        // instantiate our message manager if the application wants one
        String bpath = getMessageBundlePath();
        if (bpath != null) {
            _msgmgr = new MessageManager(bpath);
        }
    }

    /**
     * Returns true if the supplied URI should be handled by this
     * application.
     */
    public boolean matches (String uri)
    {
        return uri.startsWith(_baseURI);
    }

    /**
     * Given a request URI this generates the classname of the logic class
     * that should handle the request.
     */
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
    protected MessageManager _msgmgr;

    /**
     * This is the default file extension.
     */
    protected static final String FILE_EXTENSION = ".wm";
}
