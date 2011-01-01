//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

import java.awt.event.InputEvent;

import static com.samskivert.Log.log;

/**
 * <cite>Write once, run anywhere.</cite> Well, at least that's what it
 * said in the brochures. For those less than fresh days, you might need
 * to use this class to work around bugs on particular operating systems.
 */
public class RunAnywhere
{
    /**
     * Returns true if we're running in a JVM that identifies its
     * operating system as Windows.
     */
    public static final boolean isWindows ()
    {
        return _isWindows;
    }

    /**
     * Returns true if we're running in a JVM that identifies its
     * operating system as MacOS.
     */
    public static final boolean isMacOS ()
    {
        return _isMacOS;
    }

    /**
     * Returns true if we're running in a JVM that identifies its
     * operating system as Linux.
     */
    public static final boolean isLinux ()
    {
        return _isLinux;
    }

    /**
     * Returns {@link System#currentTimeMillis}, but works around a bug on
     * WinXP that causes time to sometimes leap into the past.
     */
    public static final long currentTimeMillis ()
    {
        long stamp = System.currentTimeMillis();

        // on WinXP the time sometimes seems to leap into the past; here
        // we do what we can to work around this insanity by simply
        // stopping time rather than allowing it to go into the past
        if (stamp < _lastStamp) {
            // only warn once per time anomaly
            if (stamp > _lastWarning) {
                log.warning("Someone call Einstein! The clock is running backwards",
                            "dt", (stamp - _lastStamp));
                _lastWarning = _lastStamp;
            }
            stamp = _lastStamp;
        }
        _lastStamp = stamp;

        return stamp;
    }

    /**
     * Returns the timestamp associated with the supplied event except on
     * the Macintosh where it returns {@link System#currentTimeMillis}
     * because {@link InputEvent#getWhen} returns completely incorrect
     * values. Hopefully this method will become unnecessary soon.
     * We also no longer trust windows, since it seems to misbehave sometimes.
     */
    public static long getWhen (InputEvent event)
    {
        return (isWindows() || isMacOS()) ? currentTimeMillis()
                                          : event.getWhen();
    }

    /** Flag indicating that we're on Windows; initialized when this class
     * is first loaded. */
    protected static boolean _isWindows;

    /** Flag indicating that we're on MacOS; initialized when this class
     * is first loaded. */
    protected static boolean _isMacOS;

    /** Flag indicating that we're on Linux; initialized when this class
     * is first loaded. */
    protected static boolean _isLinux;

    /** Used to ensure that the timer is sane. */
    protected static long _lastStamp, _lastWarning;

    static {
        try {
            String osname = System.getProperty("os.name");
            osname = (osname == null) ? "" : osname;
            _isWindows = (osname.indexOf("Windows") != -1);
            _isMacOS = (osname.indexOf("Mac OS") != -1 ||
                        osname.indexOf("MacOS") != -1);
            _isLinux = (osname.indexOf("Linux") != -1);
        } catch (Exception e) {
            // dang, can't grab system properties; we'll just pretend
            // we're not on any of these OSes
        }
    }
}
