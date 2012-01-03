//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A very brief formatter that shows only the message and stack trace (if any).
 */
public class TerseLogFormatter extends Formatter
{
    @Override
    public String format (LogRecord record)
    {
        StringBuffer buf = new StringBuffer();

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
        FormatterUtil.configureDefaultHandler(new TerseLogFormatter());
    }
}
