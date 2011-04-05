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
