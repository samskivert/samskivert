//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import static com.samskivert.util.UtilLog.log;

/**
 * The config class provides a unified interaface to application configuration information. See
 * {@link PrefsConfig} for an extension of Config that allows the default values to be reconfigured
 * and saved persistently.
 *
 * <p> A common pattern is to create, for each package that shares configuration information, a
 * singleton class containing a config object that is configured to load its data from a single
 * configuration file. For example:
 *
 * <pre>{@code
 * public class FooConfig {
 *     public static final String FIDDLES = "fiddles";
 *     public static Config config = new Config("com/fribitz/foo");
 * }
 * }</pre>
 *
 * which would look for <code>com/fribitz/foo.properties</code> in the classpath and serve up those
 * configuration values when requests were made from <code>FooConfig.config</code>. For example:
 *
 * <pre>{@code
 *     int fiddles = FooConfig.config.getValue(FooConfig.FIDDLES, 0);
 *     for (int ii = 0; ii < fiddles; ii++) {
 *         fiddle();
 *     }
 * }</pre>
 *
 * An even better approach involves creating accessors for all defined configuration properties:
 *
 * <pre>{@code
 * public class FooConfig {
 *     public static final String FIDDLES = "fiddles";
 *     public static Config config = new Config("com/fribitz/foo");
 *     public static int getFiddles () {
 *         return config.getValue(FIDDLES, FIDDLES_DEFAULT);
 *     }
 *     protected static final int FIDDLES_DEFAULT = 0;
 * }
 * }</pre>
 *
 * This allows the default value for <code>fiddles</code> to be specified in one place and
 * simplifies life for the caller who can now simply request <code>FooConfig.getFiddles()</code>.
 *
 * @see PrefsConfig
 */
public class Config
{
    /**
     * Constructs a new config object which will obtain configuration information from the
     * specified properties bundle.
     */
    public Config (String path)
    {
        _props = loadProperties(path, getClass().getClassLoader(), new Properties());
    }

    /**
     * Constructs a new config object which will obtain configuration information from the
     * specified properties bundle.
     *
     * @param loader the classloader to use to locate the properties file.
     */
    public Config (String path, ClassLoader loader)
    {
        _props = loadProperties(path, loader, new Properties());
    }

    /**
     * Constructs a config object which will obtain information from the supplied properties.
     */
    public Config (Properties props)
    {
        _props = props;
    }

    @Deprecated
    public Config (String path, Properties props)
    {
        this(props);
    }

    /**
     * Fetches and returns the value for the specified configuration property. If the value is not
     * specified in the associated properties file, the supplied default value is returned instead.
     * If the property specified in the file is poorly formatted (not and integer, not in proper
     * array specification), a warning message will be logged and the default value will be
     * returned.
     *
     * @param name name of the property.
     * @param defval the value to return if the property is not specified in the config file.
     *
     * @return the value of the requested property.
     */
    public int getValue (String name, int defval)
    {
        String val = _props.getProperty(name);
        if (val != null) {
            try {
                return Integer.decode(val).intValue(); // handles base 10, hex values, etc.
            } catch (NumberFormatException nfe) {
                log.warning("Malformed integer property", "name", name, "value", val);
            }
        }
        return defval;
    }

    /**
     * Fetches and returns the value for the specified configuration property. If the value is not
     * specified in the associated properties file, the supplied default value is returned instead.
     * If the property specified in the file is poorly formatted (not and integer, not in proper
     * array specification), a warning message will be logged and the default value will be
     * returned.
     *
     * @param name name of the property.
     * @param defval the value to return if the property is not specified in the config file.
     *
     * @return the value of the requested property.
     */
    public long getValue (String name, long defval)
    {
        String val = _props.getProperty(name);
        if (val != null) {
            try {
                defval = Long.parseLong(val);
            } catch (NumberFormatException nfe) {
                log.warning("Malformed long integer property", "name", name, "value", val);
            }
        }
        return defval;
    }

    /**
     * Fetches and returns the value for the specified configuration property. If the value is not
     * specified in the associated properties file, the supplied default value is returned instead.
     * If the property specified in the file is poorly formatted (not and integer, not in proper
     * array specification), a warning message will be logged and the default value will be
     * returned.
     *
     * @param name name of the property.
     * @param defval the value to return if the property is not specified in the config file.
     *
     * @return the value of the requested property.
     */
    public float getValue (String name, float defval)
    {
        String val = _props.getProperty(name);
        if (val != null) {
            try {
                defval = Float.parseFloat(val);
            } catch (NumberFormatException nfe) {
                log.warning("Malformed float property", "name", name, "value", val);
            }
        }
        return defval;
    }

    /**
     * Fetches and returns the value for the specified configuration property. If the value is not
     * specified in the associated properties file, the supplied default value is returned instead.
     * The returned value will be <code>false</code> if the config value is <code>"false"</code>
     * (case-insensitive), else the return value will be true.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified in the config file.
     *
     * @return the value of the requested property.
     */
    public boolean getValue (String name, boolean defval)
    {
        String val = _props.getProperty(name);
        if (val != null) {
            defval = !val.equalsIgnoreCase("false");
        }
        return defval;
    }

    /**
     * Fetches and returns the value for the specified configuration property. If the value is not
     * specified in the associated properties file, the supplied default value is returned instead.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified in the config file.
     *
     * @return the value of the requested property.
     */
    public String getValue (String name, String defval)
    {
        return _props.getProperty(name, defval);
    }

    /**
     * Fetches and returns the value for the specified configuration property. If the value is not
     * specified in the associated properties file, the supplied default value is returned instead.
     * If the property specified in the file is poorly formatted (not and integer, not in proper
     * array specification), a warning message will be logged and the default value will be
     * returned.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified in the config file.
     *
     * @return the value of the requested property.
     */
    public int[] getValue (String name, int[] defval)
    {
        // look up the value in the configuration file and use that to look up any overridden value
        String val = getValue(name, (String)null);
        int[] result = defval;

        // parse it into an array of ints
        if (val != null) {
            result = StringUtil.parseIntArray(val);
            if (result == null) {
                log.warning("Malformed int array property", "name", name, "value", val);
                return defval;
            }
        }

        return result;
    }

    /**
     * Fetches and returns the value for the specified configuration property. If the value is not
     * specified in the associated properties file, the supplied default value is returned instead.
     * If the property specified in the file is poorly formatted (not and integer, not in proper
     * array specification), a warning message will be logged and the default value will be
     * returned.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified in the config file.
     *
     * @return the value of the requested property.
     */
    public long[] getValue (String name, long[] defval)
    {
        // look up the value in the configuration file and use that to look up any overridden value
        String val = getValue(name, (String)null);
        long[] result = defval;

        // parse it into an array of longs
        if (val != null) {
            result = StringUtil.parseLongArray(val);
            if (result == null) {
                log.warning("Malformed int array property", "name", name, "value", val);
                return defval;
            }
        }

        return result;
    }

    /**
     * Fetches and returns the value for the specified configuration property. If the value is not
     * specified in the associated properties file, the supplied default value is returned instead.
     * If the property specified in the file is poorly formatted (not a floating point value, not
     * in proper array specification), a warning message will be logged and the default value will
     * be returned.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified in the config file.
     *
     * @return the value of the requested property.
     */
    public float[] getValue (String name, float[] defval)
    {
        // look up the value in the configuration file and use that to look up any overridden value
        String val = getValue(name, (String)null);
        float[] result = defval;

        // parse it into an array of ints
        if (val != null) {
            result = StringUtil.parseFloatArray(val);
            if (result == null) {
                log.warning("Malformed int array property", "name", name, "value", val);
                return defval;
            }
        }

        return result;
    }

    /**
     * Fetches and returns the value for the specified configuration property. If the value is not
     * specified in the associated properties file, the supplied default value is returned instead.
     * If the property specified in the file is poorly formatted (not and integer, not in proper
     * array specification), a warning message will be logged and the default value will be
     * returned.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified in the config file.
     *
     * @return the value of the requested property.
     */
    public String[] getValue (String name, String[] defval)
    {
        // look up the value in the configuration file and use that to look up any overridden value
        String val = getValue(name, (String)null);
        String[] result = defval;

        // parse it into an array of ints
        if (val != null) {
            result = StringUtil.parseStringArray(val);
            if (result == null) {
                log.warning("Malformed string array property", "name", name, "value", val);
                return defval;
            }
        }

        return result;
    }

    /**
     * Looks up the specified string-valued configuration entry, loads the class with that name and
     * instantiates a new instance of that class, which is returned.
     *
     * @param name the name of the property to be fetched.
     * @param defcname the class name to use if the property is not specified in the config file.
     *
     * @exception Exception thrown if any error occurs while loading or instantiating the class.
     */
    public Object instantiateValue (String name, String defcname)
        throws Exception
    {
        return Class.forName(getValue(name, defcname)).newInstance();
    }

    /**
     * Returns a properties object containing all configuration values that start with the supplied
     * prefix (plus a trailing "." which will be added if it doesn't already exist). The keys in
     * the sub-properties will have had the prefix stripped off.
     */
    public Properties getSubProperties (String prefix)
    {
        Properties props = new Properties();
        getSubProperties(prefix, props);
        return props;
    }

    /**
     * Fills into the supplied properties object all configuration values that start with the
     * supplied prefix (plus a trailing "." which will be added if it doesn't already exist). The
     * keys in the sub-properties will have had the prefix stripped off.
     */
    public void getSubProperties (String prefix, Properties target)
    {
        // slap a trailing dot on if necessary
        if (!prefix.endsWith(".")) {
            prefix = prefix + ".";
        }

        // build the sub-properties
        for (Iterator<String> iter = keys(); iter.hasNext(); ) {
            String key = iter.next();
            if (!key.startsWith(prefix)) {
                continue;
            }
            String value = getValue(key, (String)null);
            if (value == null) {
                continue;
            }
            target.put(key.substring(prefix.length()), value);
        }
    }

    /**
     * Returns an iterator that returns all of the configuration keys in this config object.
     */
    public Iterator<String> keys ()
    {
        HashSet<String> matches = new HashSet<String>();
        enumerateKeys(matches);
        return matches.iterator();
    }

    protected static Properties loadProperties (String path, ClassLoader loader, Properties props)
    {
        try {
            ConfigUtil.loadProperties(path + PROPS_SUFFIX, loader, props);
        } catch (FileNotFoundException fnfe) {
            log.debug("No properties file found to back config", "path", path);
        } catch (IOException ioe) {
            log.warning("Unable to load configuration", "path", path, "ioe", ioe);
        }
        return props;
    }

    protected void enumerateKeys (HashSet<String> keys)
    {
        Enumeration<?> defkeys = _props.propertyNames();
        while (defkeys.hasMoreElements()) {
            keys.add((String)defkeys.nextElement());
        }
    }

    /** Contains the default configuration information. */
    protected Properties _props;

    /** The file extension used for configuration files. */
    protected static final String PROPS_SUFFIX = ".properties";
}
