//
// $Id: Log.java,v 1.1 2001/10/03 18:32:28 mdb Exp $

package org.waywardgeeks;

/**
 * A placeholder class that contains a reference to the log object used by
 * the Venison package. This is a useful pattern to use when using the
 * samskivert logging facilities. One creates a top-level class like this
 * one that instantiates a log object with an name that identifies log
 * messages from that package and then provides static methods that
 * generate log messages using that instance. Then, classes in that
 * package need only import the log wrapper class and can easily use it to
 * generate log messages. For example:
 *
 * <pre>
 * import org.waywardgeeks.venison.Log;
 * // ...
 * Log.warning("All hell is breaking loose!");
 * // ...
 * </pre>
 *
 * @see com.samskivert.util.Log
 */
public class Log
{
    /** The static log instance configured for use by this package. */
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("venison");

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
