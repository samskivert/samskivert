//
// $Id: ExceptionMap.java,v 1.1 2001/02/13 20:00:28 mdb Exp $

package com.samskivert.webmacro;

import java.io.*;
import java.util.*;

import com.samskivert.util.StringUtil;

/**
 * The exception map is used to load the exception to error message
 * mapping information and to look up the appropriate response for a given
 * exception instance.
 *
 * @see FriendlyServlet
 */
public class ExceptionMap
{
    /**
     * Searches for the <code>exceptionmap.properties</code> file in the
     * classpath and loads it. If the file could not be found, an error is
     * reported and a default set of mappings is used.
     */
    public static synchronized void init ()
    {
	// only initialize ourselves once
	if (_keys != null) {
	    return;
	} else {
	    _keys = new ArrayList();
	    _values = new ArrayList();
	}

	// first try loading the properties file without a leading slash
	InputStream config = getStream(PROPS_NAME);
	if (config == null) {
	    // some JVMs require the leading slash, some don't
	    config = getStream("/" + PROPS_NAME);
	}

	// still no props, then we complain
	if (config == null) {
	    Log.log.warning("Unable to load " + PROPS_NAME +
			    " from CLASSPATH.");

	} else {
	    // otherwise process ye old config file.
	    try {
		// we'll do some serious jiggery pokery to leverage the
		// parsing implementation provided by
		// java.util.Properties. god bless method overloading
		Properties loader = new Properties() {
		    public Object put (Object key, Object value)
		    {
			_keys.add(key);
			_values.add(value);
			return key;
		    }
		};
		loader.load(config);

		// now cruise through and resolve the exceptions named as
		// keys and throw out any that don't appear to exist
		for (int i = 0; i < _keys.size(); i++) {
		    String exclass = (String)_keys.get(i);
		    try {
			Class cl = Class.forName(exclass);
			// replace the string with the class object
			_keys.set(i, cl);

		    } catch (Throwable t) {
			Log.log.warning("Unable to resolve exception " +
					"class. [class=" + exclass +
					", error=" + t + "].");
			_keys.remove(i);
			_values.remove(i);
			i--; // back on up a notch
		    }
		}

	    } catch (IOException ioe) {
		Log.log.warning("Error reading exception mapping file: " +
				ioe);
	    }
	}
    }

    /**
     * Looks up the supplied exception in the map and returns the most
     * specific error message available for exceptions of that class.
     *
     * @param ex The exception to resolve into an error message.
     *
     * @return The error message to which this exception maps (properly
     * populated with the message associated with this exception
     * instance).
     */
    public static String getMessage (Throwable ex)
    {
	String msg = DEFAULT_ERROR_MSG;

	for (int i = 0; i < _keys.size(); i++) {
	    Class cl = (Class)_keys.get(i);
	    if (cl.isInstance(ex)) {
		msg = (String)_values.get(i);
		break;
	    }
	}

	return StringUtil.replace(msg, MESSAGE_MARKER, ex.getMessage());
    }

    protected static InputStream getStream (String path)
    {
	// first try using the classloader that loaded us
	Class c = ExceptionMap.class;
	InputStream in = c.getResourceAsStream(path);
	if (null == in) {
	    // if that didn't work, try the system classloader
            c = Class.class;
            in = c.getResourceAsStream(path);
	}
	return in;
    }

    public static void main (String[] args)
    {
	ExceptionMap map = new ExceptionMap();
	System.out.println(map.getMessage(new Exception("Test error")));
    }

    protected static List _keys;
    protected static List _values;

    // initialize ourselves
    static { init(); }

    protected static final String PROPS_NAME = "exceptionmap.properties";
    protected static final String DEFAULT_ERROR_MSG = "Error: {m}.";
    protected static final String MESSAGE_MARKER = "{m}";
}
