//
// $Id: Log.java,v 1.1 2001/03/02 01:22:07 mdb Exp $

package com.samskivert;

/**
 * A placeholder class that contains a reference to the log object used by
 * the samskivert package.
 */
public class Log
{
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("samskivert");

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
