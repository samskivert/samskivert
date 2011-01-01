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
