//
// $Id$

package com.samskivert.util;

import java.util.Date;

import com.samskivert.Log;

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
                "---- First call (at " + new Date() + ") ----");
            return false;
        }

        Log.logStackTrace(new Exception(warning));
        Log.logStackTrace(_firstCall);
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
}
