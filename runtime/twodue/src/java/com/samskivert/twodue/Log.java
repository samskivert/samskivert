//
// $Id: Log.java,v 1.1 2002/11/08 09:14:21 mdb Exp $

package com.samskivert.twodue;

/**
 * A placeholder class that contains a reference to the log object used by
 * this package.
 */
public class Log
{
    /**
     * This is the log instance that will be used to log all messages for
     * this package.
     */
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("twodue");

    /** Convenience function. */
    public static void debug (String message)
    {
	log.debug(message);
    }

    /** Convenience function. */
    public static void info (String message)
    {
	log.info(message);
    }

    /** Convenience function. */
    public static void warning (String message)
    {
	log.warning(message);
    }

    /** Convenience function. */
    public static void logStackTrace (Throwable t)
    {
	log.logStackTrace(com.samskivert.util.Log.WARNING, t);
    }
}
