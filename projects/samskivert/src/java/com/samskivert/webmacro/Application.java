//
// $Id: Application.java,v 1.2 2001/03/04 06:25:48 mdb Exp $

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
     * If an application wishes to make use of the translation facilities
     * provided by the message manager, it can instantiate one and make it
     * available through this member function. This allows framework
     * components like the <code>MsgTool</code> to make use of the
     * application's message bundles.
     */
    public MessageManager getMessageManager ()
    {
        return null;
    }

    /**
     * The default application implementation takes the base URI and base
     * package as defined in the application declaration in the servlet
     * configuration and uses those to determine whether or not a given
     * URI maps to this application.
     */
    public void setConfig (String baseURI, String basePkg)
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

    /**
     * A convenience function for translating messages.
     */
    public final String translate (WebContext ctx, String msg)
    {
        MessageManager msgmgr = getMessageManager();
        return msgmgr.getMessage(ctx.getRequest(), msg);
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (WebContext ctx, String msg, Object arg)
    {
        MessageManager msgmgr = getMessageManager();
        return msgmgr.getMessage(ctx.getRequest(), msg, new Object[]{ arg });
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (WebContext ctx, String msg, Object arg1,
                                   Object arg2)
    {
        MessageManager msgmgr = getMessageManager();
        return msgmgr.getMessage(ctx.getRequest(), msg,
                                 new Object[]{ arg1, arg2 });
    }

    /**
     * A convenience function for translating messages.
     */
    public final String translate (WebContext ctx, String msg, Object arg1,
                                   Object arg2, Object arg3)
    {
        MessageManager msgmgr = getMessageManager();
        return msgmgr.getMessage(ctx.getRequest(), msg,
                                 new Object[]{ arg1, arg2, arg3 });
    }

    protected String _baseURI;
    protected String _basePkg;

    /**
     * This is the default file extension.
     */
    protected static final String FILE_EXTENSION = ".wm";
}
