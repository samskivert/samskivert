//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.awt.Dimension;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;

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
        try {
            // enable vt100 escape codes if requested
            String pstr = System.getProperty("log_vt100");
            if (!StringUtil.isBlank(pstr)) {
                _useVT100 = pstr.equalsIgnoreCase("true");
            }
            String wstr = System.getProperty("wrap_log");
            if (!StringUtil.isBlank(wstr)) {
                _wrapLog = wstr.equalsIgnoreCase("true");
            }

        } catch (SecurityException se) {
            // nothing to worry about
        }

        // obtain our terminal size, if possible and necessary
        if (_wrapLog) {
            obtainTermSize();
        }
    }

    public synchronized void log (
        int level, String moduleName, String message)
    {
        if (level >= getLevel(moduleName)) {
            System.err.println(formatEntry(moduleName, level, message));
        }
    }

    public synchronized void logStackTrace (int level, String moduleName, Throwable t)
    {
        if (level >= getLevel(moduleName)) {
            System.err.print(formatEntry(moduleName, level, ""));
            t.printStackTrace(System.err);
        }
    }

    public synchronized void setLevel (String moduleName, int level)
    {
        _levels.put(moduleName, level);
    }

    public synchronized void setLevel (int level)
    {
        _level = level;
        _levels.clear();
    }

    public synchronized int getLevel (String moduleName)
    {
        Integer level = _levels.get(moduleName);
        return (level == null) ? _level : level.intValue();
    }

    public synchronized int getLevel ()
    {
        return _level;
    }

    protected synchronized String formatEntry (
        String moduleName, int level, String message)
    {
        StringBuffer buf = new StringBuffer();
        _format.format(new Date(), buf, _fpos);
        buf.append(" ");
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

        // if we're not wrapping, append the message and be done with it
        if (!_wrapLog) {
            buf.append(message);
            return buf.toString();
        }

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

    /** The default log level. */
    protected int _level = Log.INFO;

    /** The levels of each module. */
    protected HashMap<String,Integer> _levels = new HashMap<String,Integer>();

    /** Used to accompany log messages with time stamps. */
    protected SimpleDateFormat _format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");

    /** Needed for the more efficient {@link
     * SimpleDateFormat#format(Date,StringBuffer,FieldPosition)}. */
    protected FieldPosition _fpos = new FieldPosition(SimpleDateFormat.DATE_FIELD);

    /** Contains the dimensions of the terminal window in which we're
     * running, if it was possible to obtain them. Otherwise, it contains
     * the default dimensions. This is used to wrap lines. */
    protected static Dimension _tdimens;

    /** Whether or not to use the vt100 escape codes. */
    protected boolean _useVT100 = false;

    /** Whether or not to wrap the log lines. */
    protected boolean _wrapLog = false;

    /** Used to tag log messages with their log level. */
    protected static final String[] LEVEL_CHARS = { ".", "?", "!" };

    /** Used to align wrapped log lines. */
    protected static final String GAP = "                          ";
}
