//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.AccessControlException;

import java.util.HashSet;
import java.util.Properties;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static com.samskivert.util.UtilLog.log;

/**
 * Extends the {@link Config} mechanism to allow the modification of configuration values, which
 * are persisted using the Java preferences system. If a property is {@link #setValue}d, it will
 * remain overridden in between invocations of the application (leveraging the benefits of the
 * pluggable preferences backends provided by the standard Java preferences facilities).
 */
public class PrefsConfig extends Config
{
    /**
     * Constructs a new config object which will obtain configuration information from the
     * specified properties bundle.
     */
    public PrefsConfig (String path)
    {
        super(path);

        // get a handle on the preferences instance that we'll use to override values in the
        // properties file
        try {
            _prefs = Preferences.userRoot().node(path);
        } catch (AccessControlException ace) {
            // security manager won't let us access prefs, no problem!
            log.info("Can't access preferences", "path", path);
            _prefs = new NullPreferences();
        }
    }

    /**
     * Constructs a config object which will obtain information from the supplied properties,
     * rooted at the specified path in the preferences hieriarchy.
     */
    public PrefsConfig (String path, Properties props)
    {
        super(props);

        try {
            _prefs = Preferences.userRoot().node(path);
        } catch (AccessControlException ace) {
            // security manager won't let us access prefs, no problem!
            log.info("Can't access preferences", "path", path);
            _prefs = new NullPreferences();
        }
    }

    /**
     * Sets the value of the specified preference, overriding the value defined in the
     * configuration files shipped with the application.
     */
    public void setValue (String name, int value)
    {
        Integer oldValue = null;
        if (_prefs.get(name, null) != null || _props.getProperty(name) != null) {
            oldValue = Integer.valueOf(_prefs.getInt(name, super.getValue(name, 0)));
        }

        _prefs.putInt(name, value);
        _propsup.firePropertyChange(name, oldValue, Integer.valueOf(value));
    }

    /**
     * Sets the value of the specified preference, overriding the value defined in the
     * configuration files shipped with the application.
     */
    public void setValue (String name, long value)
    {
        Long oldValue = null;
        if (_prefs.get(name, null) != null || _props.getProperty(name) != null) {
            oldValue = Long.valueOf(_prefs.getLong(name, super.getValue(name, 0L)));
        }

        _prefs.putLong(name, value);
        _propsup.firePropertyChange(name, oldValue, Long.valueOf(value));
    }

    /**
     * Sets the value of the specified preference, overriding the value defined in the
     * configuration files shipped with the application.
     */
    public void setValue (String name, float value)
    {
        Float oldValue = null;
        if (_prefs.get(name, null) != null || _props.getProperty(name) != null) {
            oldValue = Float.valueOf(_prefs.getFloat(name, super.getValue(name, 0f)));
        }

        _prefs.putFloat(name, value);
        _propsup.firePropertyChange(name, oldValue, Float.valueOf(value));
    }

    /**
     * Sets the value of the specified preference, overriding the value defined in the
     * configuration files shipped with the application.
     */
    public void setValue (String name, boolean value)
    {
        Boolean oldValue = null;
        if (_prefs.get(name, null) != null || _props.getProperty(name) != null) {
            oldValue = Boolean.valueOf(_prefs.getBoolean(name, super.getValue(name, false)));
        }

        _prefs.putBoolean(name, value);
        _propsup.firePropertyChange(name, oldValue, Boolean.valueOf(value));
    }

    /**
     * Sets the value of the specified preference, overriding the value defined in the
     * configuration files shipped with the application.
     */
    public void setValue (String name, String value)
    {
        String oldValue = getValue(name, (String)null);
        _prefs.put(name, value);
        _propsup.firePropertyChange(name, oldValue, value);
    }

    /**
     * Sets the value of the specified preference, overriding the value defined in the
     * configuration files shipped with the application.
     */
    public void setValue (String name, int[] value)
    {
        int[] oldValue = getValue(name, (int[])null);
        _prefs.put(name, StringUtil.toString(value, "", ""));
        _propsup.firePropertyChange(name, oldValue, value);
    }

    /**
     * Sets the value of the specified preference, overriding the value defined in the
     * configuration files shipped with the application.
     */
    public void setValue (String name, long[] value)
    {
        long[] oldValue = getValue(name, (long[])null);
        _prefs.put(name, StringUtil.toString(value, "", ""));
        _propsup.firePropertyChange(name, oldValue, value);
    }

    /**
     * Sets the value of the specified preference, overriding the value defined in the
     * configuration files shipped with the application.
     */
    public void setValue (String name, float[] value)
    {
        float[] oldValue = getValue(name, (float[])null);
        _prefs.put(name, StringUtil.toString(value, "", ""));
        _propsup.firePropertyChange(name, oldValue, value);
    }

    /**
     * Sets the value of the specified preference, overriding the value defined in the
     * configuration files shipped with the application.
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
     * Adds a listener that will be notified whenever any configuration properties are changed by a
     * call to one of the <code>set</code> methods.
     */
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        _propsup.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener previously added via a call to {@link
     * #addPropertyChangeListener}.
     */
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        _propsup.removePropertyChangeListener(listener);
    }

    /**
     * Adds a listener that will be notified whenever the specified configuration property is
     * changed by a call to the appropriate <code>set</code> method.
     */
    public void addPropertyChangeListener (String name, PropertyChangeListener listener)
    {
        _propsup.addPropertyChangeListener(name, listener);
    }

    /**
     * Removes a property change listener previously added via a call to {@link
     * #addPropertyChangeListener(String,PropertyChangeListener)}.
     */
    public void removePropertyChangeListener (String name, PropertyChangeListener listener)
    {
        _propsup.removePropertyChangeListener(name, listener);
    }

    @Override // from Config
    public int getValue (String name, int defval)
    {
        return _prefs.getInt(name, super.getValue(name, defval));
    }

    @Override // from Config
    public long getValue (String name, long defval)
    {
        return _prefs.getLong(name, super.getValue(name, defval));
    }

    @Override // from Config
    public float getValue (String name, float defval)
    {
        return _prefs.getFloat(name, super.getValue(name, defval));
    }

    @Override // from Config
    public boolean getValue (String name, boolean defval)
    {
        return _prefs.getBoolean(name, super.getValue(name, defval));
    }

    @Override // from Config
    public String getValue (String name, String defval)
    {
        return _prefs.get(name, super.getValue(name, defval));
    }

    @Override // from Config
    protected void enumerateKeys (HashSet<String> keys)
    {
        super.enumerateKeys(keys);

        try {
            for (String key : _prefs.keys()) {
                keys.add(key);
            }
        } catch (BackingStoreException bse) {
            log.warning("Unable to enumerate preferences keys", "error", bse);
        }
    }

    /** This is used if we don't have security access to the preferences
     * implementation that we'd like. */
    protected static class NullPreferences extends AbstractPreferences
    {
        public NullPreferences () {
            super(null, "");
        }
        @Override protected void putSpi (String key, String value) {
        }
        @Override protected String getSpi (String key) {
            return null;
        }
        @Override protected void removeSpi (String key) {
        }
        @Override protected void removeNodeSpi () throws BackingStoreException {
        }
        @Override protected String[] keysSpi () throws BackingStoreException {
            return new String[0];
        }
        @Override protected String[] childrenNamesSpi () throws BackingStoreException {
            return new String[0];
        }
        @Override protected AbstractPreferences childSpi (String name) {
            return null;
        }
        @Override protected void syncSpi () throws BackingStoreException {
        }
        @Override protected void flushSpi () throws BackingStoreException {
        }
    }

    /** Used to maintain configuration overrides. */
    protected Preferences _prefs;

    /** Used to support our property change mechanism. */
    protected PropertyChangeSupport _propsup = new PropertyChangeSupport(this);
}
