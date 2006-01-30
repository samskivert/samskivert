//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2004 Michael Bayne
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * A briefer formatter than the default Java {@link SimpleFormatter}.
 */
public class OneLineLogFormatter extends Formatter
{
    // documentation inherited
    public String format (LogRecord record)
    {
	StringBuffer buf = new StringBuffer();

        // append the timestamp
        _date.setTime(record.getMillis());
	_format.format(_date, buf, _fpos);

        // append the log level
	buf.append(" ");
	buf.append(record.getLevel().getLocalizedName());

        // append the log method call context
	buf.append(" ");
        String where = record.getSourceClassName();
        boolean legacy = (where.indexOf("LoggingLogProvider") != -1);
        if (where != null && !legacy) {
            String logger = record.getLoggerName();
            if (logger != null && where.startsWith(logger) &&
                where.length() > logger.length()) {
                where = where.substring(logger.length()+1);
            }
        } else {
            where = record.getLoggerName();
        }
        buf.append(where);
	if (record.getSourceMethodName() != null && !legacy) {
	    buf.append(".");
	    buf.append(record.getSourceMethodName());
	}

        // append the message itself
	buf.append(": ");
	buf.append(formatMessage(record));
        buf.append(LINE_SEPARATOR);

        // if an exception was also provided, append that
	if (record.getThrown() != null) {
	    try {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        record.getThrown().printStackTrace(pw);
	        pw.close();
		buf.append(sw.toString());
	    } catch (Exception ex) {
	    }
	}

	return buf.toString();
    }

    /**
     * Configures the default logging handler to use an instance of this
     * formatter when formatting messages.
     */
    public static void configureDefaultHandler ()
    {
        Logger logger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = logger.getHandlers();
        OneLineLogFormatter formatter = new OneLineLogFormatter();
        for (int ii = 0; ii < handlers.length; ii++) {
            handlers[ii].setFormatter(formatter);
        }
    }

    protected Date _date = new Date();
    protected SimpleDateFormat _format =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
    protected FieldPosition _fpos =
        new FieldPosition(SimpleDateFormat.DATE_FIELD);

    protected static String LINE_SEPARATOR = "\n";
    protected static final String DATE_FORMAT = "{0,date} {0,time}";

    static {
        try {
            LINE_SEPARATOR = System.getProperty("line.separator");
        } catch (Exception e) {
        }
    }
}
