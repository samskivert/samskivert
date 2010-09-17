//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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

import java.util.Arrays;

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
                _log.append(args[ii]).append("=");
                try {
                    Object arg = args[ii + 1];
                    _log.append(((arg == null) || !arg.getClass().isArray()) ? arg : arrayStr(arg));
                } catch (Throwable t) {
                    _log.append("<toString() failure: " + t + ">");
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

    /**
     * Return the String representation of the specified Object, which must be an array.
     */
    protected String arrayStr (Object arg)
    {
        if (arg instanceof Object[]) {
            return Arrays.deepToString((Object[])arg); // go deep, baby

        } else if (arg instanceof int[]) {
            return Arrays.toString((int[])arg);

        } else if (arg instanceof byte[]) {
            return Arrays.toString((byte[])arg);

        } else if (arg instanceof char[]) {
            return Arrays.toString((char[])arg);

        } else if (arg instanceof short[]) {
            return Arrays.toString((short[])arg);

        } else if (arg instanceof long[]) {
            return Arrays.toString((long[])arg);

        } else if (arg instanceof float[]) {
            return Arrays.toString((float[])arg);

        } else if (arg instanceof double[]) {
            return Arrays.toString((double[])arg);

        } else if (arg instanceof boolean[]) {
            return Arrays.toString((boolean[])arg);
        }
        throw new AssertionError("Not an array: " + arg);
    }

    protected boolean _hasArgs;
    protected StringBuilder _log;
}
