//
// $Id: Log.java,v 1.3 2001/03/01 21:06:22 mdb Exp $

package com.samskivert.webmacro;

/**
 * A placeholder class that contains a reference to the log object used by
 * the webmacro package.
 */
public class Log
{
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("com.samskivert.webmacro");

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
