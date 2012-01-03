//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.logging.Level;

/**
 * A ResultListener that does nothing on success and logs a warning message on failure, that's all.
 */
public class ComplainingListener<T> extends FailureListener<T>
{
    /**
     * Creates a listener that will log failures to the supplied logger.
     *
     * @param logger the logger to which to log failures.
     * @param errorText the text to log when the error is received.
     * @param args to log along with the error text. See {@link Logger#format} for info on how
     * they will be formatted.
     */
    public ComplainingListener (Logger logger, String errorText, Object... args)
    {
        this(errorText, args);
        _slogger = logger;
    }

    /**
     * Creates a listener that will log failures to the supplied logger.
     *
     * @param logger the logger to which to log failures.
     * @param errorText the text to log when the error is received.
     * @param args to log along with the error text. See {@link Logger#format} for info on how
     * they will be formatted.
     */
    public ComplainingListener (java.util.logging.Logger logger, String errorText, Object... args)
    {
        this(errorText, args);
        _jlogger = logger;
    }

    // from interface ResultListener
    public void requestFailed (Exception cause)
    {
        Object[] args = _args != null ? ArrayUtil.append(_args, cause) : new Object[] { cause };
        if (_slogger != null) {
            _slogger.warning(_errorText, args);
        } else if (_jlogger != null) {
            _jlogger.log(Level.WARNING, Logger.format(_errorText, args), cause);
        } else {
            System.err.println(Logger.format(_errorText, args));
        }
    }

    protected ComplainingListener (String errorText, Object[] args)
    {
        if (args != null && args.length % 2 == 1) {
            throw new IllegalArgumentException(
                "Got odd number of arguments (" + args.length + "). " +
                "args must be a list of key/value pairs.");
        }
        _errorText = errorText;
        _args = args;
    }

    /** The log to which we'll log our error, may be null. */
    protected Logger _slogger;

    /** The logger to which we'll log our error, may be null. */
    protected java.util.logging.Logger _jlogger;

    /** The text to output if the error happens. */
    protected String _errorText;

    /** Key-value pairs for extra information about the error. */
    protected Object[] _args;
}
