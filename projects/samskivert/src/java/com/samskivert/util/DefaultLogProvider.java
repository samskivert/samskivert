//
// $Id: DefaultLogProvider.java,v 1.2 2000/12/07 06:13:59 mdb Exp $

package com.samskivert.util;

import java.util.Hashtable;

/**
 * If no log provider is registered with the log services, the default
 * provider will be used. The default provider simple logs messages to
 * <code>System.err</code> and manages log levels in a simplistic way.
 *
 * @see Log
 * @see LogProvider
 */
public class DefaultLogProvider implements LogProvider
{
    public void log (int level, String moduleName, String message)
    {
	Integer tlevel = (Integer)_levels.get(moduleName);
	if ((tlevel != null && level >= tlevel.intValue()) ||
	    (level >= _level)) {
	    System.err.println(moduleName + ": " + message);
	}
    }

    public void logStackTrace (int level, String moduleName, Throwable t)
    {
	Integer tlevel = (Integer)_levels.get(moduleName);
	if ((tlevel != null && level >= tlevel.intValue()) ||
	    (level >= _level)) {
	    System.err.println(moduleName + ": " + t.getMessage());
	    t.printStackTrace(System.err);
	}
    }

    public void setLevel (String moduleName, int level)
    {
	_levels.put(moduleName, new Integer(level));
    }

    public void setLevel (int level)
    {
	_level = level;
	_levels.clear();
    }

    /** The default log level. */
    protected int _level = Log.INFO;

    /** The levels of each module. */
    protected Hashtable _levels = new Hashtable();
}
