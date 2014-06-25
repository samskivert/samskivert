//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * The log services provide debug, info and warning message logging
 * capabilities for a set of modules. These log services are designed to
 * provide the basic functionality needed by the samskivert codebase. It
 * is expected that the {@link LogProvider} interface will be used to
 * map these log services onto whatever more general purpose logging
 * framework is in use by the user of the samskivert codebase.
 *
 * <p> The log provider can be set via a system property to ensure that it
 * is in effect as soon as the log services are used. When invoking your
 * JVM, specify <code>-Dlog_provider=classname</code> and it will
 * automatically be instantiated and put into effect by the log services.
 * If your log provider needs more sophisticated configuration, you'll
 * have to instantiate it yourself and set it using {@link
 * #setLogProvider}.
 */
public final class Log
{
    /** Log level constant for debug entries. */
    public static final int DEBUG = 0;

    /** Log level constant for info entries. */
    public static final int INFO = 1;

    /** Log level constant for warning entries. */
    public static final int WARNING = 2;

    /**
     * Constructs a new log object with the supplied module name.
     */
    public Log (String moduleName)
    {
        _moduleName = moduleName;

        // try setting our default log level for this package
        try {
            String lstr = System.getProperty("log_level:" + moduleName);
            if (!StringUtil.isBlank(lstr)) {
                int level = levelFromString(lstr);
                if (level != -1) {
                    _provider.setLevel(moduleName, level);
                } else {
                    _provider.log(WARNING, moduleName,
                                  "Unknown log level requested " +
                                  "[level=" + lstr + "].");
                }
            }

        } catch (SecurityException se) {
            // ignore security exceptions; we're just running in a JVM
            // that won't let us read system properties
        }
    }

    /**
     * Logs the specified message at the debug level if such messages are
     * enabled.
     */
    public void debug (String message)
    {
        _provider.log(DEBUG, _moduleName, message);
    }

    /**
     * Logs the specified message at the info level if such messages are
     * enabled.
     */
    public void info (String message)
    {
        _provider.log(INFO, _moduleName, message);
    }

    /**
     * Logs the specified message at the warning level if such messages
     * are enabled.
     */
    public void warning (String message)
    {
        _provider.log(WARNING, _moduleName, message);
    }

    /**
     * Logs the stack trace of the supplied throwable at the specified
     * level (if the current log level for this module is at or below the
     * specified level).
     */
    public void logStackTrace (int level, Throwable t)
    {
        _provider.logStackTrace(level, _moduleName, t);
    }

    /**
     * Sets the log level of the specified module to the specified
     * value. The log level indicates which messages are logged and which
     * are not. For example, if the level was set to warning, then only
     * warning and error messages would be logged because info and debug
     * messages have a 'lower' log level.
     *
     * <p> Note: the log provider implementation may choose to propagate
     * the supplied level to all modules that are contained by this module
     * in the module hierarchy. For example, setting the "swing.util"
     * module to debug could also set the "swing.util.TaskMaster" level to
     * debug because it is contained by the specified module. </p>
     */
    public static void setLevel (String moduleName, int level)
    {
        _provider.setLevel(moduleName, level);
    }

    /**
     * Sets the log level for all modules to the specified level.
     */
    public static void setLevel (int level)
    {
        _provider.setLevel(level);
    }

    /**
     * Returns the log level of the specified module.
     */
    public static int getLevel (String moduleName)
    {
        return _provider.getLevel(moduleName);
    }

    /**
     * Returns the default log level for all modules.
     */
    public static int getLevel ()
    {
        return _provider.getLevel();
    }

    /**
     * Returns the log provider currently in use by the logging services.
     */
    public static LogProvider getLogProvider ()
    {
        return _provider;
    }

    /**
     * Instructs the logging services to use the specified log provider to
     * perform the actual logging.
     */
    public static void setLogProvider (LogProvider provider)
    {
        _provider = provider;
    }

    /**
     * Returns the log level that matches the specified string or -1 if
     * the string could not be interpretted as a log level.
     */
    public static int levelFromString (String level)
    {
        if (level.equalsIgnoreCase("debug")) {
            return DEBUG;
        } else if (level.equalsIgnoreCase("info")) {
            return INFO;
        } else if (level.equalsIgnoreCase("warning")) {
            return WARNING;
        } else {
            return -1;
        }
    }

    /**
     * Returns a string representation of the specified log level.
     */
    public static String levelToString (int level)
    {
        switch (level) {
        case DEBUG: return "DEBUG";
        case INFO: return "INFO";
        case WARNING: return "WARNING";
        default: return "UNKNOWN";
        }
    }

    /**
     * Checks the <code>log_provider</code> system property to see if a
     * log provider has been specified. Installs it if so. Checks the
     * <code>log_level</code> system property to see if a default log
     * level has been specified. Sets it if so.
     */
    protected static void inferConfiguration ()
    {
        // first set up our provider
        String provider = null;
        try {
            provider = System.getProperty("log_provider");
            if (provider != null) {
                Class<?> lpclass = Class.forName(provider);
                _provider = (LogProvider)lpclass.newInstance();
            }

        } catch (SecurityException se) {
            // ignore security exceptions; we're just running in a JVM
            // that won't let us read system properties

        } catch (Exception e) {
            _provider.log(WARNING, "samskivert",
                          "Error instantating log provider " +
                          "[class=" + provider + ", error=" + e + "].");
        }

        // now set our log level
        try {
            String lstr = System.getProperty("log_level");
            if (!StringUtil.isBlank(lstr)) {
                int level = levelFromString(lstr);
                if (level != -1) {
                    _provider.setLevel(level);
                } else {
                    _provider.log(WARNING, "samskivert",
                                  "Unknown log level requested " +
                                  "[level=" + lstr + "].");
                }
            }

        } catch (SecurityException se) {
            // ignore security exceptions; we're just running in a JVM
            // that won't let us read system properties
        }
    }

    /** The name of the module to which this log instance is associated. */
    protected String _moduleName;

    /** The log provider currently in use by the log services. */
    protected static LogProvider _provider = new DefaultLogProvider();

    // read our configuration when the class is loaded
    static {
        inferConfiguration();
    }
}
