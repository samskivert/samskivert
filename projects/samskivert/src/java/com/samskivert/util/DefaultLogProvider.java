//
// $Id: DefaultLogProvider.java,v 1.3 2001/08/11 22:43:29 mdb Exp $
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
