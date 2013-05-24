//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;
import java.text.FieldPosition;

import java.util.Calendar;
import java.util.Date;

import static com.samskivert.util.UtilLog.log;

/**
 * Used by various services to generate audit logs which can be useful for auditing, debugging and
 * other logly necessities. The audit logger automatically rolls over its logs at midnight to
 * facilitate the collection, processing and possible archiving of the logs.
 */
public class AuditLogger
{
    /**
     * Creates an audit logger that logs to the specified file.
     */
    public AuditLogger (File path, String filename)
    {
        this(new File(path, filename));
    }

    /**
     * Creates an audit logger that logs to the specified file.
     */
    public AuditLogger (File fullpath)
    {
        _logPath = fullpath;
        openLog(true);

        // update the day format
        _dayStamp = _dayFormat.format(new Date());
        scheduleNextRolloverCheck();
    }

    /**
     * Builds a log entry out of the given message and args using
     * {@link Logger#format(Object, Object...)}, prefixes it with the current date and time,
     * appends a newline to it and writes it to the log.
     */
    public synchronized void log (String message, Object...args)
    {
        // construct the message
        message = Logger.format(message, args);
        StringBuffer buf = new StringBuffer(message.length() + TIMESTAMP_LENGTH);
        _format.format(new Date(), buf, _fpos);
        buf.append(message);

        // and write it to the log
        boolean wrote = false;
        if (_logWriter != null) {
            _logWriter.println(buf.toString());
            wrote = !_logWriter.checkError();
        }

        // log an error if we failed to write the log message
        if (!wrote) {
            // be careful about logging zillions of errors if something bad happens to our log file
            if (_throttle.throttleOp()) {
                _throttled++;
            } else {
                if (_throttled > 0) {
                    log.warning("Suppressed " + _throttled + " intervening error messages.");
                    _throttled = 0;
                }
                log.warning("Failed to write audit log message", "file", _logPath, "msg", message);
            }
        }
    }

    /**
     * Closes this audit log (generally only done when the server is shutting down.
     */
    public synchronized void close ()
    {
        if (_logWriter != null) {
            log("log_closed");
            _logWriter.close();
            _logWriter = null;
        }
    }

    /**
     * Opens our log file, sets up our print writer and writes a message to it indicating that it
     * was opened.
     */
    protected void openLog (boolean freakout)
    {
        try {
            // create our file writer to which we'll log
            FileOutputStream fout = new FileOutputStream(_logPath, true);
            OutputStreamWriter writer = new OutputStreamWriter(fout, "UTF8");
            _logWriter = new PrintWriter(new BufferedWriter(writer), true);

            // log a standard message
            log("log_opened " + _logPath);

        } catch (IOException ioe) {
            String errmsg = "Unable to open audit log '" + _logPath + "'";
            if (freakout) {
                throw new RuntimeException(errmsg, ioe);
            } else {
                log.warning(errmsg, "ioe", ioe);
            }
        }
    }

    /**
     * Check to see if it's time to roll over the log file.
     */
    protected synchronized void checkRollOver ()
    {
        // check to see if we should roll over the log
        String newDayStamp = _dayFormat.format(new Date());

        // hey! we need to roll it over!
        if (!newDayStamp.equals(_dayStamp)) {
            log("log_closed");
            _logWriter.close();
            _logWriter = null;

            // rename the old file
            String npath = _logPath.getPath() + "." + _dayStamp;
            if (!_logPath.renameTo(new File(npath))) {
                log.warning("Failed to rename audit log file", "path", _logPath, "npath", npath);
            }

            // open our new log file
            openLog(false);

            // and set the next day stamp
            _dayStamp = newDayStamp;
        }

        scheduleNextRolloverCheck();
    }

    /**
     * Schedule the next check to see if we should roll the logs over.
     */
    protected void scheduleNextRolloverCheck ()
    {
        Calendar cal = Calendar.getInstance();

        // schedule the next check for the next hour mark
        long nextCheck = (1000L - cal.get(Calendar.MILLISECOND)) +
            (59L - cal.get(Calendar.SECOND)) * 1000L +
            (59L - cal.get(Calendar.MINUTE)) * (1000L * 60L);

        _rollover.schedule(nextCheck);
    }

    /** The interval that rolls over the log file. */
    protected Interval _rollover = new Interval(Interval.RUN_DIRECT) {
        @Override public void expired () {
            checkRollOver();
        }
    };

    /** The path to our log file. */
    protected File _logPath;

    /** We actually write to this feller here. */
    protected PrintWriter _logWriter;

    /** Suppress freakouts if our log file becomes hosed. */
    protected Throttle _throttle = new Throttle(2, 5*60*1000L);

    /** Used to count the number of throttled messages for reporting. */
    protected int _throttled;

    /** The daystamp of the log file we're currently writing to. */
    protected String _dayStamp;

    /** Used to format log file suffixes. */
    protected SimpleDateFormat _dayFormat = new SimpleDateFormat("yyyyMMdd");

    /** Used to format timestamps. */
    protected SimpleDateFormat _format = new SimpleDateFormat(TIMESTAMP_FORMAT);

    /** Annoying parameter required by the Format.format() method that appends to a string
     * buffer. */
    protected FieldPosition _fpos = new FieldPosition(SimpleDateFormat.DATE_FIELD);

    /** Timestamp format used on all log messages. */
    protected static final String TIMESTAMP_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS ";

    /** The length of the timestamp format. */
    protected static final int TIMESTAMP_LENGTH = TIMESTAMP_FORMAT.length();
}
