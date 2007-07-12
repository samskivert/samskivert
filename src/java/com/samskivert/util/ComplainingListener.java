//
// $Id$

package com.samskivert.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ResultListener that does nothing on success and logs a warning message on failure, that's all.
 */
public class ComplainingListener<T>
    implements ResultListener<T>
{
    public ComplainingListener (Log log, String errorText)
    {
        _log = log;
        _errorText = errorText;
    }

    public ComplainingListener (Logger logger, String errorText)
    {
        _logger = logger;
        _errorText = errorText;
    }

    // documentation inherited from interface ResultListener
    public void requestCompleted (T result) { /* nada */ }

    // documentation inherited from interface ResultListener
    public void requestFailed (Exception cause)
    {
        if (_log != null) {
            _log.warning(_errorText + " [cause=" + cause + "].");
        } else if (_logger != null) {
            _logger.log(Level.WARNING, _errorText, cause);
        } else {
            System.err.println(_errorText + " [cause=" + cause + "].");
        }
    }

    /** The log to which we'll log our error, may be null. */
    protected Log _log;

    /** The logger to which we'll log our error, may be null. */
    protected Logger _logger;

    /** The text to output if the error happens. */
    protected String _errorText;
}
