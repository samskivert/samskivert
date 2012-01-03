//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * The log provider interface allows the simple logging services provided
 * to the samskivert codebase to be mapped onto an actual logging
 * framework.
 */
public interface LogProvider
{
    /**
     * Log a message at the specified level for the specified module, if
     * messages are enabled for that particular combination.
     */
    public void log (int level, String moduleName, String message);

    /**
     * Log the stack trace of the supplied throwable at the specified
     * level for the specified module, if messages are enabled for that
     * particular combination.
     */
    public void logStackTrace (int level, String moduleName, Throwable t);

    /**
     * Set the log level for the specified module to the specified
     * level. The log services assume that all messages at or higher than
     * the specified level will be logged.
     */
    public void setLevel (String moduleName, int level);

    /**
     * Set the log level for all modules to the specified level. The log
     * services assume that all messages at or higher than the specified
     * level will be logged.
     */
    public void setLevel (int level);

    /**
     * Returns the log level for the specified module.
     */
    public int getLevel (String moduleName);

    /**
     * Returns the default log level for all modules.
     */
    public int getLevel ();
}
