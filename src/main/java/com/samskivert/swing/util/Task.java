//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

/**
 * A swing application requests that a task be invoked by the task
 * master. The task master invokes the task on a separate thread and
 * informs the swing application when the task has completed via the task
 * observer interface. It does so in a way that plays nicely with the
 * swing event dispatch thread (the swing application is notified of the
 * task's completion on the event dispatch thread so that it can wiggle
 * its user interface elements to its heart's desire).
 */
public interface Task
{
    /**
     * This method is called by the task master to invoke the task. The
     * task should run to completion and return some value.
     */
    public Object invoke () throws Exception;

    /**
     * This method is called by the task master when it has received a
     * request to cancel this task. If the task can be cancelled, abort
     * should return true and the currently running call to
     * <code>invoke()</code> should immediately return (the return value
     * will be ignored). If the task cannot be cancelled, abort should
     * return false and the task master will simply abandon the task and
     * the thread on which it is running.
     */
    public boolean abort ();
}
