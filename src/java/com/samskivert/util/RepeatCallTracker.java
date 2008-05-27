//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import static com.samskivert.Log.log;

/**
 * A class used to debug situations where a method that should be called
 * once and only once is being called again later and one wishes to report
 * the stack trace of the first and current calling on any repeat
 * callings. <em>Note:</em> this object is not thread safe. If the
 * multiple callings may take place on different threads, you must
 * synchronize the calls to {@link #checkCall} yourself.
 */
public class RepeatCallTracker
{
    /**
     * This method should be called when the code passes through the code
     * path that should be called only once. The first time through this
     * path, the method will return false and record the stack trace.
     * Subsquent calls will log an error and report the current and
     * first-time-through stack traces.
     *
     * @param warning the warning message to issue prior to logging the
     * two stack traces.
     */
    public boolean checkCall (String warning)
    {
        if (_firstCall == null) {
            _firstCall = new Exception(
                "---- First call (at " + _format.format(new Date()) + ") ----");
            return false;
        }

        log.warning(warning, new Exception());
        log.warning("First call:", _firstCall);
        return true;
    }

    /**
     * Resets this repeat call tracker.
     */
    public void clear ()
    {
        _firstCall = null;
    }

    /** Used to keep the stack trace around from the first call. */
    protected Exception _firstCall;

    /** Used to report the first call time with a detailed time stamp. */
    protected SimpleDateFormat _format =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
}
