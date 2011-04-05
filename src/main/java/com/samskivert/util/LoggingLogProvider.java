//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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
