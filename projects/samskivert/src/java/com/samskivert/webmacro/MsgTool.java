//
// $Id: MsgTool.java,v 1.1 2001/03/03 21:21:28 mdb Exp $

package com.samskivert.webmacro;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.webmacro.*;
import org.webmacro.servlet.WebContext;
import org.webmacro.util.PropertyObject;
import org.webmacro.util.PropertyMethod;

import com.samskivert.Log;

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

	    // first fetch our messages resource bundle
	    final ResourceBundle messages =
		ResourceBundle.getBundle(MESSAGE_FILE_NAME);

	    // then create a wrapper that allows webmacro to access it
	    return new Wrapper(messages);

	} catch (MissingResourceException mre) {
	    Log.warning("Unable to load message bundle: " + mre);
	    return new Wrapper(null);

	} catch (ClassCastException cce) {
	    throw new InvalidContextException("error.requires_webcontext");
	}
    }

    public void destroy (Object obj)
    {
	// nothing to clean up
    }

    public static class Wrapper implements PropertyObject
    {
	public Wrapper (ResourceBundle bundle)
	{
	    _bundle = bundle;
	}

	public Object getProperty (Context context, Object[] names, int offset)
	    throws PropertyException, SecurityException
	{
	    StringBuffer path = new StringBuffer();
	    boolean bogus = false;
	    int lastidx = names.length-1;

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

	    // look up the translated message
	    String msg = _bundle.getString(path.toString());

	    // if the last component is a property method, we want to
	    // substitute it's arguments into the returned string using a
	    // message formatter
	    if (names[lastidx] instanceof PropertyMethod) {
		PropertyMethod pm = (PropertyMethod)names[lastidx];
		// we may cache message formatters later, but for now just
		// use the static convenience function
		Object[] args = pm.getArguments(context);
		msg = MessageFormat.format(msg, args);
	    }

	    return msg;
	}

	public boolean setProperty (Context context, Object[] names, int offset,
				    Object value)
	    throws PropertyException, SecurityException
	{
	    throw new PropertyException("Setting a message resource " +
					"is not supported!");
	}

	protected ResourceBundle _bundle;
    }

    /**
     * The file name of the resource file that contains our translation
     * strings.
     */
    protected static final String MESSAGE_FILE_NAME = "messages";
}
