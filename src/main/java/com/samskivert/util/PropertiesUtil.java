//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Properties;

import com.samskivert.io.StreamUtil;

/**
 * Utility functions related to properties objects.
 */
public class PropertiesUtil
{
    /**
     * Extracts all properties from the supplied properties object with the specified prefix,
     * removes the prefix from the key for those properties and inserts them into a new properties
     * object which is then returned. This is useful for extracting properties from a global
     * configuration object that must be passed to a service that expects it's own private
     * properties (JDBC for example).
     *
     * The property file might look like so:
     *
     * <pre>
     * my_happy_param=my_happy_value
     * ...
     * jdbc.driver=foo.bar.Driver
     * jdbc.url=jdbc://blahblah
     * jdbc.username=bob
     * jdbc.password=is your uncle
     * ...
     * my_happy_other_param=my_happy_other_value
     * </pre>
     *
     * This can be supplied to <code>getSubProperties()</code> with a prefix of <code>"jdbc"</code>
     * and the following properties would be returned:
     *
     * <pre>
     * driver=foo.bar.Driver
     * url=jdbc://blahblah
     * username=bob
     * password=is your uncle
     * </pre>
     */
    public static Properties getSubProperties (Properties source, String prefix)
    {
        Properties dest = new Properties();
        extractSubProperties(source, dest, prefix);
        return dest;
    }

    /**
     * Like {@link #getSubProperties(Properties,String)} with the additional functionality of
     * loading up defaults for the sub-properties which are identified by the
     * <code>defaultsPrefix</code> string.
     */
    public static Properties getSubProperties (
        Properties source, String prefix, String defaultsPrefix)
    {
        // first load up the defaults
        Properties defs = new Properties();
        extractSubProperties(source, defs, defaultsPrefix);

        // now load up the desired properties
        Properties dest = new Properties(defs);
        extractSubProperties(source, dest, prefix);

        return dest;
    }

    /**
     * Returns a filtered version of the specified properties that first looks for a property
     * starting with the given prefix, then looks for a property without the prefix. For example,
     * passing the prefix "alt" and using the following properties:
     *
     * <pre>
     * alt.texture = sand.png
     * lighting = off
     * </pre>
     *
     * ...would return "sand.png" for the property "texture" and "off" for the property "lighting".
     * Unlike {@link #getSubProperties}, the object returned by this method references, rather than
     * copies, the underlying properties. Only the {@link Properties#getProperty} methods are
     * guaranteed to work correctly on the returned object.
     */
    public static Properties getFilteredProperties (final Properties source, String prefix)
    {
        final String dprefix = prefix + ".";
        return new Properties() {
            @Override public String getProperty (String key) {
                return getProperty(key, null);
            }
            @Override public String getProperty (String key, String defaultValue) {
                return source.getProperty(dprefix + key, source.getProperty(key, defaultValue));
            }
            @Override public Enumeration<?> propertyNames () {
                return new Enumeration<Object>() {
                    public boolean hasMoreElements () {
                        return next != null;
                    }
                    public Object nextElement () {
                        Object onext = next;
                        next = findNext();
                        return onext;
                    }
                    protected Object findNext () {
                        while (senum.hasMoreElements()) {
                            String name = (String)senum.nextElement();
                            if (!name.startsWith(dprefix)) {
                                return name;
                            }
                            name = name.substring(dprefix.length());
                            if (!source.containsKey(name)) {
                                return name;
                            }
                        }
                        return null;
                    }
                    protected Enumeration<?> senum = source.propertyNames();
                    protected Object next = findNext();
                };
            }
        };
    }

    /**
     * A helper function used by the {@link #getSubProperties} methods.
     */
    protected static void extractSubProperties (Properties source, Properties dest, String prefix)
    {
        // extend the prefix to contain a dot
        prefix = prefix + ".";
        int preflen = prefix.length();

        // scan the source properties
        Enumeration<?> names = source.propertyNames();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            // skip unrelated properties
            if (!name.startsWith(prefix)) {
                continue;
            }
            // insert the value into the new properties minus the prefix
            dest.put(name.substring(preflen), source.getProperty(name));
        }
    }

    /**
     * Reads and parses the supplied file into a {@link Properties} instance.
     */
    public static Properties load (File from)
        throws IOException
    {
        Properties props = new Properties();
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(from));
        try {
            props.load(bin);
            return props;
        } finally {
            StreamUtil.close(bin);
        }
    }

    /**
     * Loads up the supplied properties file and returns the specified key. Clearly this is an
     * expensive operation and you should load a properties file separately if you plan to retrieve
     * multiple keys from it. This method, however, is convenient for, say, extracting a value from
     * a properties file that contains only one key, like a build timestamp properties file, for
     * example.
     *
     * @return the value of the key in question or null if no such key exists or an error occurred
     * loading the properties file.
     */
    public static String loadAndGet (File propFile, String key)
    {
        try {
            return load(propFile).getProperty(key);
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * Like {@link #loadAndGet(File,String)} but obtains the properties data via the classloader.
     *
     * @return the value of the key in question or null if no such key exists or an error occurred
     * loading the properties file.
     */
    public static String loadAndGet (String loaderPath, String key)
    {
        try {
            Properties props = ConfigUtil.loadProperties(loaderPath);
            return props.getProperty(key);
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * Returns the specified property from the supplied properties object.
     * @throws MissingPropertyException with the text "Missing required property
     * '<code>key</code>'." if the property does not exist or is the empty string.
     */
    public static String requireProperty (Properties props, String key)
    {
        return requireProperty(props, key, "Missing required property '" + key + "'.");
    }

    /**
     * Returns the specified property from the supplied properties object.
     * @throws MissingPropertyException with the supplied message if the property does not exist or
     * is the empty string.
     */
    public static String requireProperty (Properties props, String key, String missingMessage)
    {
        String value = props.getProperty(key);
        if (StringUtil.isBlank(value)) {
            throw new MissingPropertyException(key, missingMessage);
        }
        return value;
    }
}
