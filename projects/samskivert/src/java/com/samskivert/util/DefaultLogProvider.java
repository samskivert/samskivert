//
// $Id: DefaultLogProvider.java,v 1.5 2002/05/31 20:45:26 mdb Exp $
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

import java.text.SimpleDateFormat;
import java.util.Date;
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
    public synchronized void log (
        int level, String moduleName, String message)
    {
	Integer tlevel = (Integer)_levels.get(moduleName);
	if ((tlevel != null && level >= tlevel.intValue()) ||
	    (level >= _level)) {
	    System.err.println(formatEntry(moduleName, level, message));
	}
    }

    public synchronized void logStackTrace (
        int level, String moduleName, Throwable t)
    {
	Integer tlevel = (Integer)_levels.get(moduleName);
	if ((tlevel != null && level >= tlevel.intValue()) ||
	    (level >= _level)) {
	    System.err.println(formatEntry(moduleName, level, t.getMessage()));
	    t.printStackTrace(System.err);
	}
    }

    public synchronized void setLevel (String moduleName, int level)
    {
	_levels.put(moduleName, new Integer(level));
    }

    public synchronized void setLevel (int level)
    {
	_level = level;
	_levels.clear();
    }

    protected synchronized String formatEntry (
        String moduleName, int level, String message)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(_format.format(new Date())).append(" ");
        if (level == Log.WARNING) {
            buf.append(BOLD);
        } else if (level == Log.DEBUG) {
            buf.append(REVERSE);
        }
        buf.append(LEVEL_CHARS[level]).append(" ");

        // let the formatting continue to influence the module name and
        // then it will be turned off when underlining is turned off

        // if we wrap, we include the module name in the wrapped text
        int wrapwid = LOG_LINE_LENGTH - buf.length();
        buf.append(UNDERLINE).append(moduleName).append(PLAIN).append(": ");

        // we'll be wrapping the log lines
        int remain = message.length(), offset = 0;
        int linewid = Math.min(LOG_LINE_LENGTH - buf.length(), remain);

        // append the first line
        buf.append(message.substring(offset, offset+linewid));
        remain -= linewid;
        offset += linewid;

        // now append the wrapped lines (if there are any)
        while (remain > 0) {
            buf.append("\n").append(GAP);
            linewid = Math.min(wrapwid, remain);
            buf.append(message.substring(offset, offset+linewid));
            remain -= linewid;
            offset += linewid;
        }

        return buf.toString();
    }

    /** The default log level. */
    protected int _level = Log.INFO;

    /** The levels of each module. */
    protected Hashtable _levels = new Hashtable();

    /** Used to accompany log messages with time stamps. */
    protected SimpleDateFormat _format =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSSS");

    /** Used to tag log messages with their log level. */
    protected static final String[] LEVEL_CHARS = { ".", "?", "!" };

    /** Used to align wrapped log lines. */
    protected static final String GAP = "                           ";

    /** VT100 formatting code to enabled bold text. */
    protected static final String BOLD = "\033[1m";

    /** VT100 formatting code to enabled reverse video text. */
    protected static final String REVERSE = "\033[7m";

    /** VT100 formatting code to enabled underlined text. */
    protected static final String UNDERLINE = "\033[4m";

    /** VT100 formatting code to revert to plain text. */
    protected static final String PLAIN = "\033[m";

    /** Maximum log line length. */
    protected static final int LOG_LINE_LENGTH = 132;
}
