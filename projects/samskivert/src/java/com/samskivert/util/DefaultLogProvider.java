//
// $Id: DefaultLogProvider.java,v 1.16 2002/10/16 19:10:13 shaper Exp $
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

import java.awt.Dimension;

import java.io.PrintStream;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import com.samskivert.io.ExtensiblePrintStream;

/**
 * If no log provider is registered with the log services, the default
 * provider will be used. The default provider simply logs messages to
 * <code>System.err</code> and manages log levels in a simplistic way.
 *
 * @see Log
 * @see LogProvider
 */
public class DefaultLogProvider implements LogProvider
{
    /**
     * Constructs a default log provider.
     */
    public DefaultLogProvider ()
    {
        // obtain our terminal size, if possible
        obtainTermSize();

        try {
            // enable vt100 escape codes if requested
            String pstr = System.getProperty("log_vt100");
            if (!StringUtil.blank(pstr)) {
                _useVT100 = pstr.equalsIgnoreCase("true");
            }

        } catch (SecurityException se) {
            // nothing to worry about
        }
    }

    /**
     * Sets whether recent log messages should be cached and made
     * available via {@link #getRecentMessages}.  The default setting is
     * <code>false</code>.  Note that currently all messages logged after
     * caching is turned on will be kept forever (or until caching is
     * turned off.)
     */
    public synchronized void setCacheMessages (boolean cache)
    {
        if (cache && _recent == null) {
            // create the list of recent messages
            _recent = new ArrayList();

            // swap in our own custom output streams for stdout and stderr
            // in order to cache all future output
            _oout = System.out;
            _oerr = System.err;
            System.setOut(new CachingPrintStream(_oout));
            System.setErr(new CachingPrintStream(_oerr));

        } else if (!cache && _recent != null) {
            // clear out the recent message list
            _recent = null;

            // restore the original output streams
            System.setOut(_oout);
            System.setErr(_oerr);
        }
    }

    /**
     * Returns a list of the recent log messages, or <code>null</code> if
     * log messages are not currently being cached.
     */
    public synchronized List getRecentMessages ()
    {
        return _recent;
    }

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
        if (level == Log.WARNING && _useVT100) {
            buf.append(TermUtil.BOLD);
        } else if (level == Log.DEBUG && _useVT100) {
            buf.append(TermUtil.REVERSE);
        }
        buf.append(LEVEL_CHARS[level]).append(" ");

        // append the thread id
        // buf.append(Thread.currentThread().hashCode() % 1000).append(" ");

        // make sure the message isn't wack
        if (message == null) {
            message = "null";
        }

        // let the formatting continue to influence the module name and
        // then it will be turned off when underlining is turned off

        // we include the module name on the first line
        buf.append(_useVT100 ? TermUtil.UNDERLINE : "").append(moduleName);
        buf.append(_useVT100 ? TermUtil.PLAIN : "").append(": ");

        // if the text contains newlines, use those instead of wrapping
        if (message.indexOf("\n") != -1) {
            String[] lines = StringUtil.split(message, "\n");
            for (int ii = 0; ii < lines.length; ii++) {
                if (ii > 0) {
                    buf.append("\n").append(GAP);
                }
                buf.append(lines[ii]);
            }

        } else {
            // we'll be wrapping the log lines
            int wrapwid = _tdimens.width - GAP.length();
            int remain = message.length(), offset = 0;

            // our first line contains the module name (and a colon and
            // space) which must be accountded for when wrapping
            int lwid = Math.min(wrapwid - moduleName.length() - 2, remain);
            // int lwid = Math.min(wrapwid - moduleName.length() - 6, remain);

            // append the first line
            buf.append(message.substring(offset, offset+lwid));
            remain -= lwid;
            offset += lwid;

            // now append the wrapped lines (if there are any)
            while (remain > 0) {
                buf.append("\n").append(GAP);
                lwid = Math.min(wrapwid, remain);
                buf.append(message.substring(offset, offset+lwid));
                remain -= lwid;
                offset += lwid;
            }
        }

        return buf.toString();
    }

    /**
     * Attempts to obtain the dimensions of the terminal window in which
     * we're running. This is extremely platform specific, but feel free
     * to add code to do the right thing for your platform.
     */
    protected static void obtainTermSize ()
    {
        if (_tdimens == null) {
            _tdimens = TermUtil.getTerminalSize();

            // if we were unable to obtain our dimensions, use defaults
            if (_tdimens == null) {
                _tdimens = new Dimension(132, 24);
            }
        }
    }

    /**
     * A print stream that caches all printed text in the {@link #_recent}
     * list.
     */
    protected class CachingPrintStream extends ExtensiblePrintStream
    {
        /**
         * Constructs a caching print stream that caches all output
         * written to the supplied stream.
         */
        public CachingPrintStream (PrintStream s)
        {
            super(s, true);
        }

        // documentation inherited
        public void handlePrinted (String s)
        {
            _buf.append(s);
        }

        // documentation inherited
        public void handleNewLine ()
        {
            // save off the text
            _recent.add(_buf.toString());
            // clear out the line buffer
            _buf = new StringBuffer();
        }

        /** The working string that gathers each log message line. */
        protected StringBuffer _buf = new StringBuffer();
    }

    /** Whether or not to use the vt100 escape codes. */
    protected boolean _useVT100 = false;

    /** The default log level. */
    protected int _level = Log.INFO;

    /** The levels of each module. */
    protected Hashtable _levels = new Hashtable();

    /** Used to accompany log messages with time stamps. */
    protected SimpleDateFormat _format =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");

    /** The cached recent log messages that are made available to any
     * external entities that would like access to them if we're actually
     * caching messages. */
    protected ArrayList _recent;

    /** The original output streams that we save off if we're caching
     * recent log messages. */
    protected PrintStream _oout, _oerr;

    /** Contains the dimensions of the terminal window in which we're
     * running, if it was possible to obtain them. Otherwise, it contains
     * the default dimensions. This is used to wrap lines. */
    protected static Dimension _tdimens;

    /** Used to tag log messages with their log level. */
    protected static final String[] LEVEL_CHARS = { ".", "?", "!" };

    /** Used to align wrapped log lines. */
    protected static final String GAP = "                          ";
}
