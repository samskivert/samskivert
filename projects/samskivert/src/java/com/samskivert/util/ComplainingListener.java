//
// $Id$

package com.samskivert.util;


/**
 * A ResultListener that does nothing on success and logs a warning
 * message on failure, that's all.
 */
public class ComplainingListener
    implements ResultListener
{
    public ComplainingListener (Log log, String errorText)
    {
        _log = log;
        _errorText = errorText;
    }

    // documentation inherited from interface ResultListener
    public void requestCompleted (Object result) { /* nada */ }

    // documentation inherited from interface ResultListener
    public void requestFailed (Exception cause)
    {
        _log.warning(_errorText + " [cause=" + cause + "].");
    }

    /** The log to which we'll log our error. */
    protected Log _log;

    /** The text to output if the error happens. */
    protected String _errorText;
}
