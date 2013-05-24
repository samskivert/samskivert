//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.util;

import java.io.*;
import java.util.*;

import com.samskivert.util.ConfigUtil;

import static com.samskivert.servlet.Log.log;

/**
 * The exception map is used to map exceptions to error messages based on
 * a static, server-wide configuration.
 *
 * <p>The configuration file is loaded via the classpath. The file should
 * be named <code>exceptionmap.properties</code> and placed in the
 * classpath of the JVM in which the servlet is executed. The file should
 * contain colon-separated mappings from exception classes to friendly
 * error messages. For example:
 *
 * <pre>
 * # Exception mappings (lines beginning with # are ignored)
 * com.samskivert.servlet.util.FriendlyException: An error occurred while \
 * processing your request: {m}
 *
 * # lines ending with \ are continued on the next line
 * java.sql.SQLException: The database is currently unavailable. Please \
 * try your request again later.
 *
 * java.lang.Exception: An unexpected error occurred while processing \
 * your request. Please try again later.
 * </pre>
 *
 * The message associated with the exception will be substituted into the
 * error string in place of <code>{m}</code>. The exceptions should be
 * listed in order of most to least specific, as the first mapping for
 * which the exception to report is an instance of the listed exception
 * will be used.
 *
 * <p><em>Note:</em> These exception mappings will generally be used for
 * all requests (perhaps some day only for requests associated with a
 * particular application). Regardless, this error handling mechanism
 * should not be used for request specific errors. For example, an SQL
 * exception reporting a duplicate key should probably be caught and
 * reported specifically by the appropriate populator (it can still
 * leverage the pattern of inserting the error message into the context as
 * <code>"error"</code>) rather than relying on the default SQL exception
 * error message which is not likely to be meaningful for such a
 * situation.
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
            _keys = new ArrayList<Class<?>>();
            _values = new ArrayList<String>();
        }

        // first try loading the properties file without a leading slash
        ClassLoader cld = ExceptionMap.class.getClassLoader();
        InputStream config = ConfigUtil.getStream(PROPS_NAME, cld);
        if (config == null) {
            log.warning("Unable to load " + PROPS_NAME + " from CLASSPATH.");

        } else {
            // otherwise process ye old config file.
            try {
                // we'll do some serious jiggery pokery to leverage the parsing
                // implementation provided by java.util.Properties. god bless
                // method overloading
                final ArrayList<String> classes = new ArrayList<String>();
                Properties loader = new Properties() {
                    @Override public Object put (Object key, Object value) {
                        classes.add((String)key);
                        _values.add((String)value);
                        return key;
                    }
                };
                loader.load(config);

                // now cruise through and resolve the exceptions named as
                // keys and throw out any that don't appear to exist
                for (int i = 0; i < classes.size(); i++) {
                    String exclass = classes.get(i);
                    try {
                        Class<?> cl = Class.forName(exclass);
                        // replace the string with the class object
                        _keys.add(cl);

                    } catch (Throwable t) {
                        log.warning("Unable to resolve exception class.", "class", exclass,
                                    "error", t);
                        _values.remove(i);
                        i--; // back on up a notch
                    }
                }

            } catch (IOException ioe) {
                log.warning("Error reading exception mapping file: " + ioe);
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
            Class<?> cl = _keys.get(i);
            if (cl.isInstance(ex)) {
                msg = _values.get(i);
                break;
            }
        }
        return msg.replace(MESSAGE_MARKER, ex.getMessage());
    }

    protected static List<Class<?>> _keys;
    protected static List<String> _values;

    // initialize ourselves
    static { init(); }

    protected static final String PROPS_NAME = "exceptionmap.properties";
    protected static final String DEFAULT_ERROR_MSG = "Error: {m}";
    protected static final String MESSAGE_MARKER = "{m}";
}
