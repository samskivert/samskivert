//
// $Id: Interval.java,v 1.5 2004/02/25 13:20:44 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

import java.util.Timer;
import java.util.TimerTask;

import com.samskivert.Log;

/**
 * An interface for doing operations after some delay.
 */
public abstract class Interval
{
    /**
     * Create a simple interval that does not use a RunQueue to run
     * the expire() method.
     */
    public Interval ()
    {
    }

    /**
     * Create an Interval that uses the specified RunQueue to run
     * the expire() method.
     */
    public Interval (RunQueue runQueue)
    {
        if (runQueue == null) {
            throw new NullPointerException("RunQueue cannot be null, " +
                "use other constructor if you want a simple Interval.");
        }
        _runQueue = runQueue;
    }

    /**
     *
     * The main method where your interval should do its work.
     *
     */
    public abstract void expired ();

    /**
     * Schedule the interval to execute once, after the specified delay.
     */
    public final void schedule (long delay)
    {
        schedule(delay, 0L);
    }

    /**
     * Schedule the interval to execute repeatedly, with the same delay.
     */
    public final void schedule (long delay, boolean repeat)
    {
        schedule(delay, repeat ? delay : 0L);
    }

    /**
     * Schedule the interval to execute repeatedly with the specified
     * initial delay and repeat delay.
     */
    public final synchronized void schedule (
            long initialDelay, long repeatDelay)
    {
        cancel();
        _task = new IntervalTask();

        if (repeatDelay == 0L) {
            _timer.schedule(_task, initialDelay);
        } else {
            _timer.scheduleAtFixedRate(_task, initialDelay, repeatDelay);
        }
    }

    /**
     * Cancel the Interval, and ensure that any expirations that are queued
     * up but have not yet run do not run.
     */
    public final synchronized void cancel ()
    {
        if (_task != null) { // task can only be null if we were never scheduled
            _task.cancel();
            _task = null;
        }
    }

    /**
     * Safely expire the interval.
     */
    protected final void safelyExpire (TimerTask task)
    {
        // only expire the interval if the task is still valid
        if (_task == task) {
            try {
                expired();
            } catch (Throwable t) {
                Log.warning("Interval broken in expired(): " + t);
                Log.logStackTrace(t);
            }
        }
    }

    /**
     * The task that schedules actually runs the interval.
     */
    protected class IntervalTask extends TimerTask
        implements Runnable
    {
        // inherited from both TimerTask and Runnable
        public void run () {
            if (_runQueue == null || _runQueue.isDispatchThread()) {
                safelyExpire(this);
            } else {
                _runQueue.postRunnable(this);
            }
        }
    }

    /** If non-null, the RunQueue used to run the expired() method for each
     * Interval. */
    protected RunQueue _runQueue;

    /** The task that actually schedules our execution with the static Timer. */
    protected volatile TimerTask _task;

    /** The daemon timer used to schedule all Intervals. */
    protected static final Timer _timer =
        new Timer(/*JDK1.5 "samskivert Interval Timer",*/ true);
}
