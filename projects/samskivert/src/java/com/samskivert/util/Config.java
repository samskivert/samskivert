//
// $Id: Config.java,v 1.25 2004/02/25 13:20:44 mdb Exp $
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.AccessControlException;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.samskivert.Log;

/**
 * The config class provides a unified interaface to application
 * configuration information. It takes care of loading properties files
 * (done via the classpath) and allows for overriding and inheriting of
 * properties values (see {@link ConfigUtil#loadInheritedProperties}).
 *
 * <p> A common pattern is to create, for each package that shares
 * configuration information, a singleton class containing a config object
 * that is configured to load its data from a single configuration
 * file. For example:
 *
 * <pre>
 * public class FooConfig
 * {
 *     public static final String FIDDLES = "fiddles";
 *
 *     public static Config config = new Config("com/fribitz/foo");
 * }
 * </pre>
 *
 * which would look for <code>com/fribitz/foo.properties</code> in the
 * classpath and serve up those configuration values when requests were
 * made from <code>FooConfig.config</code>. For example:
 *
 * <pre>
 *     int fiddles = FooConfig.config.getValue(FooConfig.FIDDLES, 0);
 *     for (int ii = 0; ii < fiddles; ii++) {
 *         fiddle();
 *     }
 * </pre>
 *
 * An even better approach involves creating accessors for all defined
 * configuration properties:
 *
 * <pre>
 * public class FooConfig
 * {
 *     public static final String FIDDLES = "fiddles";
 *
 *     public static Config config = new Config("com/fribitz/foo");
 *
 *     public static int getFiddles ()
 *     {
 *         return config.getValue(FIDDLES, FIDDLES_DEFAULT);
 *     }
 *
 *     protected static final int FIDDLES_DEFAULT = 0;
 * }
 * </pre>
 *
 * This allows the default value for <code>fiddles</code> to be specified
 * in one place and simplifies life for the caller who can now simply
 * request <code>FooConfig.getFiddles()</code>.
 *
 * <p> The config class allows one to override configuration values
 * persistently, using the standard Java {@link Preferences} facilities to
 * maintain the overridden values. If a property is {@link #setValue}d in
 * a configuration object, it will remain overridden in between
 * invocations of the application (leveraging the benefits of the
 * pluggable preferences backends provided by the standard Java
 * preferences facilities).
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
        _props = new Properties();

        try {
            // append the file suffix onto the path
            String ppath = path + PROPS_SUFFIX;

            // load the properties file
            ConfigUtil.loadInheritedProperties(ppath, _props);

        } catch (FileNotFoundException fnfe) {
            Log.debug("No properties file found to back config " +
                      "[path=" + path + "].");

        } catch (IOException ioe) {
            Log.warning("Unable to load configuration [path=" + path +
                        ", ioe=" + ioe + "].");
        }

        // get a handle on the preferences instance that we'll use to
        // override values in the properties file
        try {
            _prefs = Preferences.userRoot().node(path);
        } catch (AccessControlException ace) {
            // security manager won't let us access prefs, no problem!
            _prefs = new NullPreferences();
        }
    }

    /**
     * Constructs a config object which will obtain information from the
     * supplied properties, rooted at the specified path in the
     * preferences hieriarchy.
     */
    public Config (String path, Properties props)
    {
        _props = props;
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
        return _prefs.getInt(name, getDefValue(name, defval));
    }

    /**
     * Returns the value specified in the properties override file.
     */
    protected int getDefValue (String name, int defval)
    {
        String val = _props.getProperty(name);
        if (val != null) {
            try {
                // handle hex values
                if (val.startsWith("0x") || val.startsWith("0X")) {
                    defval = Integer.parseInt(val.substring(2), 16);
                } else {
                    defval = Integer.parseInt(val);
                }
            } catch (NumberFormatException nfe) {
                Log.warning("Malformed integer property [name=" + name +
                            ", value=" + val + "].");
            }
        }
        return defval;
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, int value)
    {
        Integer oldValue = null;
        if (_prefs.get(name, null) != null ||
            _props.getProperty(name) != null) {
            oldValue = new Integer(_prefs.getInt(name, getDefValue(name, 0)));
        }

        _prefs.putInt(name, value);
        _propsup.firePropertyChange(name, oldValue, new Integer(value));
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
        return _prefs.getLong(name, getDefValue(name, defval));
    }

    /**
     * Returns the value specified in the properties override file.
     */
    protected long getDefValue (String name, long defval)
    {
        String val = _props.getProperty(name);
        if (val != null) {
            try {
                defval = Long.parseLong(val);
            } catch (NumberFormatException nfe) {
                Log.warning("Malformed long integer property [name=" + name +
                            ", value=" + val + "].");
            }
        }
        return defval;
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, long value)
    {
        Long oldValue = null;
        if (_prefs.get(name, null) != null ||
            _props.getProperty(name) != null) {
            oldValue = new Long(_prefs.getLong(name, getDefValue(name, 0L)));
        }

        _prefs.putLong(name, value);
        _propsup.firePropertyChange(name, oldValue, new Long(value));
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
    public float getValue (String name, float defval)
    {
        return _prefs.getFloat(name, getDefValue(name, defval));
    }

    /**
     * Returns the value specified in the properties override file.
     */
    protected float getDefValue (String name, float defval)
    {
        String val = _props.getProperty(name);
        if (val != null) {
            try {
                defval = Float.parseFloat(val);
            } catch (NumberFormatException nfe) {
                Log.warning("Malformed float property [name=" + name +
                            ", value=" + val + "].");
            }
        }
        return defval;
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, float value)
    {
        Float oldValue = null;
        if (_prefs.get(name, null) != null ||
            _props.getProperty(name) != null) {
            oldValue = new Float(_prefs.getFloat(name, getDefValue(name, 0f)));
        }

        _prefs.putFloat(name, value);
        _propsup.firePropertyChange(name, oldValue, new Float(value));
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
        String oldValue = getValue(name, (String)null);
        _prefs.put(name, value);
        _propsup.firePropertyChange(name, oldValue, value);
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
        return _prefs.getBoolean(name, getDefValue(name, defval));
    }

    /**
     * Returns the value specified in the properties override file.
     */
    protected boolean getDefValue (String name, boolean defval)
    {
        String val = _props.getProperty(name);
        if (val != null) {
            defval = !val.equalsIgnoreCase("false");
        }
        return defval;
    }

    /**
     * Sets the value of the specified preference, overriding the value
     * defined in the configuration files shipped with the application.
     */
    public void setValue (String name, boolean value)
    {
        Boolean oldValue = null;
        if (_prefs.get(name, null) != null ||
            _props.getProperty(name) != null) {
            oldValue = new Boolean(
                _prefs.getBoolean(name, getDefValue(name, false)));
        }

        _prefs.putBoolean(name, value);
        _propsup.firePropertyChange(name, oldValue, new Boolean(value));
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
        int[] oldValue = getValue(name, (int[])null);
        _prefs.put(name, StringUtil.toString(value, "", ""));
        _propsup.firePropertyChange(name, oldValue, value);
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
    public long[] getValue (String name, long[] defval)
    {
        // look up the value in the configuration file and use that to
        // look up any overridden value
        String val = _prefs.get(name, _props.getProperty(name));
        long[] result = defval;

        // parse it into an array of longs
        if (val != null) {
            result = StringUtil.parseLongArray(val);
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
    public void setValue (String name, long[] value)
    {
        long[] oldValue = getValue(name, (long[])null);
        _prefs.put(name, StringUtil.toString(value, "", ""));
        _propsup.firePropertyChange(name, oldValue, value);
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead. If
     * the property specified in the file is poorly formatted (not a
     * floating point value, not in proper array specification), a warning
     * message will be logged and the default value will be returned.
     *
     * @param name the name of the property to be fetched.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public float[] getValue (String name, float[] defval)
    {
        // look up the value in the configuration file and use that to
        // look up any overridden value
        String val = _prefs.get(name, _props.getProperty(name));
        float[] result = defval;

        // parse it into an array of ints
        if (val != null) {
            result = StringUtil.parseFloatArray(val);
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
    public void setValue (String name, float[] value)
    {
        float[] oldValue = getValue(name, (float[])null);
        _prefs.put(name, StringUtil.toString(value, "", ""));
        _propsup.firePropertyChange(name, oldValue, value);
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
        String[] oldValue = getValue(name, (String[])null);
        _prefs.put(name, StringUtil.joinEscaped(value));
        _propsup.firePropertyChange(name, oldValue, value);
    }

    /**
     * Remove any set value for the specified preference.
     */
    public void remove (String name)
    {
        // we treat the old value as a String, I hope that's ok!
        String oldValue = getValue(name, (String) null);
        _prefs.remove(name);
        _propsup.firePropertyChange(name, oldValue, null);
    }

    /**
     * Adds a listener that will be notified whenever any configuration
     * properties are changed by a call to one of the <code>set</code>
     * methods.
     */
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        _propsup.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener previously added via a call to
     * {@link #addPropertyChangeListener}.
     */
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        _propsup.removePropertyChangeListener(listener);
    }

    /**
     * Adds a listener that will be notified whenever the specified
     * configuration property is changed by a call to the appropriate
     * <code>set</code> method.
     */
    public void addPropertyChangeListener (
        String name, PropertyChangeListener listener)
    {
        _propsup.addPropertyChangeListener(name, listener);
    }

    /**
     * Removes a property change listener previously added via a call to
     * {@link #addPropertyChangeListener(String,PropertyChangeListener)}.
     */
    public void removePropertyChangeListener (
        String name, PropertyChangeListener listener)
    {
        _propsup.removePropertyChangeListener(name, listener);
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

    /** This is used if we don't have security access to the preferences
     * implementation that we'd like. */
    protected static class NullPreferences extends AbstractPreferences
    {
        public NullPreferences () {
            super(null, "");
        }
        protected void putSpi (String key, String value) {
        }
        protected String getSpi (String key) {
            return null;
        }
        protected void removeSpi (String key) {
        }
        protected void removeNodeSpi () throws BackingStoreException {
        }
        protected String[] keysSpi () throws BackingStoreException {
            return new String[0];
        }
        protected String[] childrenNamesSpi () throws BackingStoreException {
            return new String[0];
        }
        protected AbstractPreferences childSpi (String name) {
            return null;
        }
        protected void syncSpi () throws BackingStoreException {
        }
        protected void flushSpi () throws BackingStoreException {
        }
    }

    /** Contains the default configuration information. */
    protected Properties _props;

    /** Used to maintain configuration overrides. */
    protected Preferences _prefs;

    /** Used to support our property change mechanism. */
    protected PropertyChangeSupport _propsup = new PropertyChangeSupport(this);

    /** The file extension used for configuration files. */
    protected static final String PROPS_SUFFIX = ".properties";
}
