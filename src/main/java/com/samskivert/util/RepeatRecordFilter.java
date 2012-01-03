//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Filters repeated log messages, emitting "Previous message repeated N times."
 * when a non-repeated message is eventually logged. Log record equality is
 * determined by comparing the record's logging level, message text and stack
 * trace (if provided).
 */
public class RepeatRecordFilter
    implements Filter
{
    /**
     * Configures the default logging handlers to use an instance of this
     * filter.
     */
    public static void configureDefaultHandler (int maxRepeat)
    {
        Logger logger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = logger.getHandlers();
        RepeatRecordFilter filter = new RepeatRecordFilter(maxRepeat);
        for (int ii = 0; ii < handlers.length; ii++) {
            handlers[ii].setFilter(filter);
        }
    }

    public RepeatRecordFilter (int maxRepeat)
    {
        _maxCount = maxRepeat;
    }

    public boolean isLoggable (LogRecord record)
    {
        if (_previous != null && _count < _maxCount &&
            record.getLevel() == _previous.getLevel() &&
            ObjectUtil.equals(record.getMessage(), _previous.getMessage()) &&
            ObjectUtil.equals(record.getThrown(), _previous.getThrown())) {
            _count++;
            return false;
        }
        if (_count > 0) {
            // we have to do things in this order because we're going to recurse
            String msg = "Previous message repeated " + _count + " times.";
            _count = 0;
            LogManager.getLogManager().getLogger(record.getLoggerName()).log(
                _previous.getLevel(), msg);
        }
        _previous = record;
        return true;
    }

    protected int _maxCount, _count;
    protected LogRecord _previous;
}
