//
// $Id: MsgTool.java,v 1.2 2001/03/04 06:15:39 mdb Exp $

package com.samskivert.webmacro;

import org.webmacro.*;
import org.webmacro.servlet.WebContext;
import org.webmacro.util.PropertyObject;
import org.webmacro.util.PropertyMethod;

import com.samskivert.Log;
import com.samskivert.servlet.MessageManager;

/**
 * The message tool maps a set of appliation messages (translation
 * strings) into the context so that they are available to templates that
 * wish to display localized text.
 */
public class MsgTool implements ContextTool
{
    /**
     * Loads up the message resources and inserts them into the context.
     */
    public Object init (Context ctx)
	throws InvalidContextException
    {
	try {
	    WebContext wctx = (WebContext)ctx;

            // get a handle on the application in effect for this request
            Application app = DispatcherServlet.getApplication(wctx);
            if (app == null) {
                String err = "No application in effect for this request. " +
                    "Can't resolve messages without an application.";
                throw new InvalidContextException(err);
            }

            // get the message manager from the application
            MessageManager msgmgr = app.getMessageManager();
            if (msgmgr == null) {
                String err = "Application did not provide a message " +
                    "manager. Can't resolve messages without one.";
                throw new InvalidContextException(err);
            }

	    // then create a wrapper that allows webmacro to access it
	    return new Wrapper(msgmgr);

	} catch (ClassCastException cce) {
	    throw new InvalidContextException("MsgTool requires a WebContext");
	}
    }

    public void destroy (Object obj)
    {
	// nothing to clean up
    }

    public static class Wrapper implements PropertyObject
    {
	public Wrapper (MessageManager msgmgr)
	{
	    _msgmgr = msgmgr;
	}

	public Object getProperty (Context context, Object[] names, int offset)
	    throws PropertyException, SecurityException
	{
	    StringBuffer path = new StringBuffer();
	    boolean bogus = false;
	    int lastidx = names.length-1;
            WebContext wctx = (WebContext)context;

	    // reconstruct the path to the property
	    for (int i = offset; i < names.length; i++) {
		// separate components with dots
		if (path.length() > 0) {
		    path.append(".");
		}

		// each name should either be a PropertyMethod or a string
		// or an object that can be toString()ed
		if (names[i] instanceof PropertyMethod) {
		    // make a note to freak out if this is anything but
		    // the last component of the path. we'd freak out now
		    // but we want to reconstruct the path so that we can
		    // report it in the exception that's thrown
		    if (i != lastidx) {
			bogus = true;
		    }
		    path.append(((PropertyMethod)names[i]).getName());

		} else {
		    path.append(names[i]);
		}
	    }

	    // do any pending freaking out
	    if (bogus) {
		throw new PropertyException("Invalid message resource " +
					    "path: " + path);
	    }

	    // if the last component is a property method, we want to use
	    // it's arguments when looking up the message
	    if (names[lastidx] instanceof PropertyMethod) {
		PropertyMethod pm = (PropertyMethod)names[lastidx];
		// we may cache message formatters later, but for now just
		// use the static convenience function
		Object[] args = pm.getArguments(context);
                return _msgmgr.getMessage(wctx.getRequest(),
                                          path.toString(), args);

	    } else {
                // otherwise just look up the path
                return _msgmgr.getMessage(wctx.getRequest(), path.toString());
            }
	}

	public boolean setProperty (Context context, Object[] names, int offset,
				    Object value)
	    throws PropertyException, SecurityException
	{
	    throw new PropertyException("Setting a message resource " +
					"is not supported!");
	}

	protected MessageManager _msgmgr;
    }
}
