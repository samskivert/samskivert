//
// $Id: Log.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides convenient access to our logging instance.
 */
public class Log
{
    /** A reference to our logger. */
    public static Logger log = Logger.getLogger("com.samskivert.depends");

    /**
     * Passes through to {@link Logger#fine(String)}.
     */
    public static void fine (String message)
    {
        log.fine(message);
    }

    /**
     * Passes through to {@link Logger#info(String)}.
     */
    public static void info (String message)
    {
        log.info(message);
    }

    /**
     * Passes through to {@link Logger#warning(String)}.
     */
    public static void warning (String message)
    {
        log.warning(message);
    }

    /**
     * Passes through to {@link Logger#log} with level
     * <code>WARNING</code>.
     */
    public static void warning (String message, Exception e)
    {
        log.log(Level.WARNING, message, e);
    }

    /**
     * Passes through to {@link Logger#severe(String)}.
     */
    public static void severe (String message)
    {
        log.severe(message);
    }

    /**
     * Passes through to {@link Logger#log} with level
     * <code>SEVERE</code>.
     */
    public static void severe (String message, Exception e)
    {
        log.log(Level.SEVERE, message, e);
    }
}
