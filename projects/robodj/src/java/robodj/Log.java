//
// $Id: Log.java,v 1.1 2001/03/18 06:58:55 mdb Exp $

package robodj;

/**
 * A placeholder class that contains a reference to the log object used by
 * the robodj package.
 */
public class Log
{
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("robodj");

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
