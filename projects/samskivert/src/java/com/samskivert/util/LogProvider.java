//
// $Id: LogProvider.java,v 1.1 2000/12/06 00:24:46 mdb Exp $

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
}
