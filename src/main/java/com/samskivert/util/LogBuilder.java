//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * Formats a message and an array of alternating key value pairs like so:
 *
 * <pre>message [key=value, key=value, key=value]</pre>
 */
public class LogBuilder
{
    /**
     * Creates a log builder with no message and no initial key value pairs.
     */
    public LogBuilder ()
    {
        this("");
    }

    /**
     * Creates a log builder with the given message and key value pairs.
     */
    public LogBuilder (Object message, Object... args)
    {
        _log = new StringBuilder().append(message);
        append(args);
    }

    /**
     * Adds the given key value pairs to the log.
     */
    public LogBuilder append (Object... args)
    {
        if (args != null && args.length > 1) {
            for (int ii = 0, nn = args.length - (args.length % 2); ii < nn; ii += 2) {
                if (_hasArgs) {
                    _log.append(", ");
                } else {
                    if (_log.length() > 0) {
                        // only need a space if we have a message
                        _log.append(' ');
                    }
                    _log.append('[');
                    _hasArgs = true;
                }
                _log.append(args[ii]).append('=');
                try {
                    _log.append(StringUtil.toString(args[ii + 1]));
                } catch (Throwable t) {
                    _log.append("<toString() failure: ").append(t).append('>');
                }
            }
        }
        return this;
    }

    /**
     * Returns the formatted log message. Does not reset the buffer. You can continue to append
     * arguments and call {@link #toString} again.
     */
    @Override public String toString ()
    {
        if (_hasArgs) {
            try {
                return _log.append(']').toString();
            } finally {
                _log.setLength(_log.length() - 1);
            }
        }
        return _log.toString();
    }

    protected boolean _hasArgs;
    protected StringBuilder _log;
}
