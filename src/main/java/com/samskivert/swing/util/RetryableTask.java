//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

import java.awt.Component;
import javax.swing.*;

/**
 * A retryable task is one that is allowed to fail at which point a dialog
 * is presented to the user asking them if they would like to retry the
 * task. In general, such practices are discouraged because the software
 * should handle retrying itself, but in cases where failure results in
 * the abandonment of a lot of work and automatic retries have already
 * been tried, it can be reasonable to give the user one last chance to
 * remedy any problems that are causing the error.
 */
public abstract class RetryableTask
{
    /**
     * This should be implemented by the retryable task user to perform
     * whatever they wish to be done in a retryable manner.
     */
    public abstract void invoke () throws Exception;

    /**
     * Invokes the supplied task and catches any thrown exceptions. In the
     * event of an exception, the provided message is displayed to the
     * user and the are allowed to retry the task or allow it to fail.
     */
    public void invokeTask (Component parent, String retryMessage)
        throws Exception
    {
        while (true) {
            try {
                invoke();
                return;

            } catch (Exception e) {
                Object[] options = new Object[] {
                    "Retry operation", "Abort operation" };
                int rv = JOptionPane.showOptionDialog(
                    parent, retryMessage + "\n\n" + e.getMessage(),
                    "Operation failure", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE, null, options, options[1]);
                if (rv == 1) {
                    throw e;
                }
            }
        }
    }
}
