//
// $Id: Config.java,v 1.15 2002/10/14 00:23:09 mdb Exp $
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

package com.samskivert.util;

import java.io.IOException;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.samskivert.Log;

/**
 * The config class provides a unified interaface to application
 * configuration information. It takes care of loading properties files
 * (done via the classpath) and merging configuration data from multiple
 * configuration files with the same path (so that users of packages can
 * override configuration settings for the packages that they use; see
 * {@ConfigUtil#loadInheritedProperties}).
 *
 * <p> The primary pattern is to create, for each package that shares
 * configuration information, a singleton class containing a config object
 * that is configured to load its data from a single configuration
 * file. For example:
 *
 * <pre>
 * public class FooConfig
 * {
 *     public static Config config = new Config("com/fribitz/foo");
 * }
 * </pre>
 *
 * which would look for <code>com/fribitz/foo.properties</code> in the
 * classpath and serve up those configuration values when requests were
 * made from <code>FooConfig.config</code>.
 *
 * <p> The config class allows for users to override configuration values
 * persistently, using the standard Java {@link Preferences} facilities to
 * maintain the overridden values. If a value is set in a configuration
 * object, it will remain overridden in between invocations of the
 * application (and generally leverage the benefits of the pluggable
 * preferences backends provided by the standard preferences stuff).
 */
public class Config
{
    /**
     * Constructs a new config object which will obtain configuration
     * information from the specified properties bundle.
     */
    public Config (String path)
    {
        // first load up our default prefs
        try {
            // append the file suffix onto the path
            String ppath = path + PROPS_SUFFIX;

            // load the properties file
            _props = ConfigUtil.loadInheritedProperties(ppath);
            if (_props == null) {
                Log.warning("Unable to locate configuration definitions " +
                            "[path=" + path + "].");
                _props = new Properties();
            }

        } catch (IOException ioe) {
            Log.warning("Unable to load configuration [path=" + path +
                        ", ioe=" + ioe + "].");
        }

        // get a handle on the preferences instance that we'll use to
        // override values in the properties file
        _prefs = Preferences.userRoot().node(path);
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead. If
     * the property specified in the file is poorly formatted (not and
     * integer, not in proper array specification), a warning message will
     * be logged and the default value will be returned.
     *
     * @param name name of the property.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public int getValue (String name, int defval)
    {
        // if there is a value, parse it into an integer
        String val = _props.getProperty(name);
        if (val != null) {
            try {
                defval = Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                Log.warning("Malformed integer property [name=" + name +
                            ", value=" + val + "].");
            }
        }

        // finally look for an overridden value
        return _prefs.getInt(name, defval);
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, int value)
    {
        _prefs.putInt(name, value);
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead. If
     * the property specified in the file is poorly formatted (not and
     * integer, not in proper array specification), a warning message will
     * be logged and the default value will be returned.
     *
     * @param name name of the property.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public long getValue (String name, long defval)
    {
        // if there is a value, parse it into an integer
        String val = _props.getProperty(name);
        if (val != null) {
            try {
                defval = Long.parseLong(val);
            } catch (NumberFormatException nfe) {
                Log.warning("Malformed long integer property [name=" + name +
                            ", value=" + val + "].");
            }
        }

        // finally look for an overridden value
        return _prefs.getLong(name, defval);
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, long value)
    {
        _prefs.putLong(name, value);
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public String getValue (String name, String defval)
    {
        // if there is a value, parse it into an integer
        String val = _props.getProperty(name);
        if (val != null) {
            defval = val;
        }

        // finally look for an overridden value
        return _prefs.get(name, defval);
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, String value)
    {
        _prefs.put(name, value);
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned
     * instead.  The returned value will be <code>false</code> if the
     * config value is <code>"false"</code> (case-insensitive), else
     * the return value will be true.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public boolean getValue (String name, boolean defval)
    {
        // if there is a value, parse it into an integer
        String val = _props.getProperty(name);
        if (val != null) {
            defval = !val.equalsIgnoreCase("false");
        }

        // finally look for an overridden value
        return _prefs.getBoolean(name, defval);
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, boolean value)
    {
        _prefs.putBoolean(name, value);
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead. If
     * the property specified in the file is poorly formatted (not and
     * integer, not in proper array specification), a warning message will
     * be logged and the default value will be returned.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public int[] getValue (String name, int[] defval)
    {
        // look up the value in the configuration file and use that to
        // look up any overridden value
        String val = _prefs.get(name, _props.getProperty(name));
        int[] result = defval;

        // parse it into an array of ints
        if (val != null) {
            result = StringUtil.parseIntArray(val);
            if (result == null) {
                Log.warning("Malformed int array property [name=" + name +
                            ", value=" + val + "].");
                return defval;
            }
        }

        return result;
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, int[] value)
    {
        _prefs.put(name, StringUtil.toString(value, "", ""));
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead. If
     * the property specified in the file is poorly formatted (not and
     * integer, not in proper array specification), a warning message will
     * be logged and the default value will be returned.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public String[] getValue (String name, String[] defval)
    {
        // look up the value in the configuration file and use that to
        // look up any overridden value
        String val = _prefs.get(name, _props.getProperty(name));
        String[] result = defval;

        // parse it into an array of ints
        if (val != null) {
            result = StringUtil.parseStringArray(val);
            if (result == null) {
                Log.warning("Malformed string array property [name=" + name +
                            ", value=" + val + "].");
                return defval;
            }
        }

        return result;
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, String[] value)
    {
        _prefs.put(name, StringUtil.joinEscaped(value));
    }

    /**
     * Looks up the specified string-valued configuration entry, loads the
     * class with that name and instantiates a new instance of that class,
     * which is returned.
     *
     * @param name the name of the property to be fetched.
     * @param defcname the class name to use if the property is not
     * specified in the config file.
     *
     * @exception Exception thrown if any error occurs while loading or
     * instantiating the class.
     */
    public Object instantiateValue (String name, String defcname)
	throws Exception
    {
	return Class.forName(getValue(name, defcname)).newInstance();
    }

    /**
     * Returns a properties object containing all configuration values
     * that start with the supplied prefix (plus a trailing "." which will
     * be added if it doesn't already exist). The keys in the
     * sub-properties will have had the prefix stripped off.
     */
    public Properties getSubProperties (String prefix)
    {
        Properties props = new Properties();
        getSubProperties(prefix, props);
        return props;
    }

    /**
     * Fills into the supplied properties object all configuration values
     * that start with the supplied prefix (plus a trailing "." which will
     * be added if it doesn't already exist). The keys in the
     * sub-properties will have had the prefix stripped off.
     */
    public void getSubProperties (String prefix, Properties target)
    {
        // slap a trailing dot on if necessary
        if (!prefix.endsWith(".")) {
            prefix = prefix + ".";
        }

        // build the sub-properties
        Iterator iter = keys();
        while (iter.hasNext()) {
            String key = (String)iter.next();
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
     * Returns an iterator that returns all of the configuration keys in
     * this config object.
     */
    public Iterator keys ()
    {
        // what with all the complicated business, we just need to take
        // the brute force approach and enumerate everything up front
        HashSet matches = new HashSet();

        // add the keys provided in the config files
        Enumeration defkeys = _props.propertyNames();
        while (defkeys.hasMoreElements()) {
            matches.add(defkeys.nextElement());
        }

        // then add the overridden keys
        try {
            String[] keys = _prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                matches.add(keys[i]);
            }
        } catch (BackingStoreException bse) {
            Log.warning("Unable to enumerate preferences keys " +
                        "[error=" + bse + "].");
        }

        return matches.iterator();
    }

    /** Contains the default configuration information. */
    protected Properties _props;

    /** Used to maintain configuration overrides. */
    protected Preferences _prefs;

    /** The file extension used for configuration files. */
    protected static final String PROPS_SUFFIX = ".properties";
}
