//
// $Id: Log.java,v 1.5 2001/08/12 01:34:31 mdb Exp $
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

/**
 * The log services provide debug, info and warning message logging
 * capabilities for a set of modules. These log services are designed to
 * provide the basic functionality needed by the samskivert codebase. It
 * is expected that the {@link LogProvider} interface will be used to
 * map these log services onto whatever more general purpose logging
 * framework is in use by the user of the samskivert codebase.
 */
public final class Log
{
    /** Log level constant for debug entries. */
    public static final int DEBUG = 0;

    /** Log level constant for info entries. */
    public static final int INFO = 0;

    /** Log level constant for warning entries. */
    public static final int WARNING = 0;

    /**
     * Constructs a new log object with the supplied module name.
     */
    public Log (String moduleName)
    {
	_moduleName = moduleName;
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
     * <p/> Note: the log provider implementation may choose to propagate
     * the supplied level to all modules that are contained by this module
     * in the module hierarchy. For example, setting the "swing.util"
     * module to debug could also set the "swing.util.TaskMaster" level to
     * debug because it is contained by the specified module.
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
     * Instructs the logging services to use the specified log provider to
     * perform the actual logging.
     */
    public static void setLogProvider (LogProvider provider)
    {
	_provider = provider;
    }

    /** The name of the module to which this log instance is associated. */
    protected String _moduleName;

    /** The log provider currently in use by the log services. */
    protected static LogProvider _provider = new DefaultLogProvider();
}
