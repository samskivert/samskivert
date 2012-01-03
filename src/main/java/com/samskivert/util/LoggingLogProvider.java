//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configures the samskivert logging routines to use the Java logging
 * facilities for logging. This provides a usable temporary bridge until
 * things that use the (now legacy) samskivert logging routines can be
 * converted to use Java's logging facilities directly.
 */
public class LoggingLogProvider
    implements LogProvider
{
    // documentation inherited from interface
    public void log (int level, String moduleName, String message)
    {
        getLogger(moduleName).log(getLevel(level), message);
    }

    // documentation inherited from interface
    public void logStackTrace (int level, String moduleName, Throwable t)
    {
        getLogger(moduleName).log(getLevel(level), "", t);
    }

    // documentation inherited from interface
    public void setLevel (String moduleName, int level)
    {
        getLogger(moduleName).setLevel(getLevel(level));
    }

    // documentation inherited from interface
    public void setLevel (int level)
    {
        getLogger(null).setLevel(getLevel(level));
    }

    // documentation inherited from interface
    public int getLevel (String moduleName)
    {
        return getLevel(getLogger(moduleName).getLevel());
    }

    // documentation inherited from interface
    public int getLevel ()
    {
        return getLevel(getLogger(null).getLevel());
    }

    protected final Logger getLogger (String moduleName)
    {
        Logger logger = Logger.getLogger("global");
        if (!StringUtil.isBlank(moduleName)) {
            logger = _loggers.get(moduleName);
            if (logger == null) {
                _loggers.put(moduleName, logger = Logger.getLogger(moduleName));
            }
        }
        return logger;
    }

    protected static final Level getLevel (int level)
    {
        switch (level) {
        case Log.DEBUG: return Level.FINE;
        case Log.WARNING: return Level.WARNING;
        default:
        case Log.INFO: return Level.INFO;
        }
    }

    protected static final int getLevel (Level level)
    {
        if (level == Level.WARNING) {
            return Log.WARNING;
        } else if (level == Level.INFO) {
            return Log.INFO;
        } else {
            return Log.DEBUG;
        }
    }

    protected HashMap<String,Logger> _loggers = new HashMap<String,Logger>();
}
