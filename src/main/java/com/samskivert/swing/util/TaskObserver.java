//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

/**
 * The task observer interface provides the means by which the task master
 * can communicate the success or failure of a task invocation back to the
 * originator of a task.
 */
public interface TaskObserver
{
    /**
     * If the task successfully runs to completion and returns a result,
     * this member function will be called on the supplied observer.
     *
     * @param name The name under which the task was originally invoked.
     * @param result The result returned by the task's
     * <code>invoke()</code> method.
     */
    public void taskCompleted (String name, Object result);

    /**
     * If the task fails to run to completion and instead throws an
     * exception, this member function will be called on the supplied
     * observer.
     *
     * @param name The name under which the task was originally invoked.
     * @param exception The exception thrown by the task during the call
     * to <code>invoke()</code>.
     */
    public void taskFailed (String name, Throwable exception);
}
