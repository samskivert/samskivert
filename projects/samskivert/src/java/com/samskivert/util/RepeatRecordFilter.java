//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2006 Michael Bayne
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
