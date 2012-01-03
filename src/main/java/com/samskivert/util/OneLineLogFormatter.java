//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * A briefer formatter than the default Java {@link SimpleFormatter}.
 */
public class OneLineLogFormatter extends Formatter
{
    /**
     * Creates a log formatter that will include the function from which a log entry was generated.
     */
    public OneLineLogFormatter ()
    {
        this(true);
    }

    /**
     * Creates a log formatter that will optionally include or not include the function from which
     * a log entry was generated.
     */
    public OneLineLogFormatter (boolean showWhere)
    {
        _showWhere = showWhere;
    }

    @Override
    public String format (LogRecord record)
    {
        StringBuffer buf = new StringBuffer();

        // append the timestamp
        _date.setTime(record.getMillis());
        _format.format(_date, buf, _fpos);

        // append the log level
        buf.append(" ");
        buf.append(record.getLevel().getLocalizedName());
        buf.append(" ");

        if (_showWhere) {
            // append the log method call context
            String where = record.getSourceClassName();
            boolean useLoggerName = true;
            if (where != null) {
                // strip the package name from the logging class
                where = where.substring(where.lastIndexOf(".")+1);
                // handle legacy log usage patterns
                useLoggerName = (where.equals("Log") || where.equals("LoggingLogProvider") ||
                                 where.startsWith("JDK14Logger$Impl"));
            }
            if (useLoggerName) {
                where = record.getLoggerName();
            }
            buf.append(where);
            if (record.getSourceMethodName() != null && !useLoggerName) {
                buf.append(".");
                buf.append(record.getSourceMethodName());
            }
            buf.append(": ");
        }

        // append the message itself
        buf.append(formatMessage(record));
        buf.append(FormatterUtil.LINE_SEPARATOR);

        // if an exception was also provided, append that
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                buf.append(sw.toString());
            } catch (Exception ex) {
                buf.append("Format failure:").append(ex);
            }
        }

        return buf.toString();
    }

    /**
     * Configures the default logging handler to use an instance of this formatter when formatting
     * messages.
     */
    public static void configureDefaultHandler ()
    {
        configureDefaultHandler(true);
    }

    /**
     * Configures the default logging handler to use an instance of this formatter when formatting
     * messages.
     */
    public static void configureDefaultHandler (boolean showWhere)
    {
        FormatterUtil.configureDefaultHandler(new OneLineLogFormatter(showWhere));
    }

    protected boolean _showWhere;
    protected Date _date = new Date();
    protected SimpleDateFormat _format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
    protected FieldPosition _fpos = new FieldPosition(SimpleDateFormat.DATE_FIELD);

    protected static final String DATE_FORMAT = "{0,date} {0,time}";
}
