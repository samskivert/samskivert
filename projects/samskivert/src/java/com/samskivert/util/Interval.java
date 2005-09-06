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
 * An interface for doing operations after some delay. Allows expiration
 * to occur on a specific thread, and guarantees that any queued expiration
 * will not run if the Interval has since been cancelled or rescheduled.
 */
public abstract class Interval
{
    /** This is used to post our interval to a run queue and provides
     * methods for getting access to the Interval derivation that is
     * actually going to do the work for profiling and reporting. */
    public static class RunQueueRunnable implements Runnable
    {
        public RunQueueRunnable (IntervalTask task, Interval interval) {
            _task = task;
            _interval = interval;
        }

        public Interval getInterval () {
            return _interval;
        }

        public void run () {
            _interval.safelyExpire(_task);
        }

        public String toString () {
            // to aid in debugging, this run unit reports its name as that
            // of the interval
            return _interval.toString();
        }

        protected IntervalTask _task;
        protected Interval _interval;
    };

    /**
     * Create a simple interval that does not use a RunQueue to run
     * the expired() method.
     */
    public Interval ()
    {
    }

    /**
     * Create an Interval that uses the specified RunQueue to run
     * the expired() method.
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
     * Supersedes any previous schedule that this Interval may have had.
     */
    public final void schedule (long delay)
    {
        schedule(delay, 0L);
    }

    /**
     * Schedule the interval to execute repeatedly, with the same delay.
     * Supersedes any previous schedule that this Interval may have had.
     */
    public final void schedule (long delay, boolean repeat)
    {
        schedule(delay, repeat ? delay : 0L);
    }

    /**
     * Schedule the interval to execute repeatedly with the specified
     * initial delay and repeat delay.
     * Supersedes any previous schedule that this Interval may have had.
     */
    public final void schedule (long initialDelay, long repeatDelay)
    {
        cancel();
        TimerTask task = _task = new IntervalTask();

        if (repeatDelay == 0L) {
            _timer.schedule(task, initialDelay);
        } else {
            _timer.scheduleAtFixedRate(task, initialDelay, repeatDelay);
        }
    }

    /**
     * Cancel the current schedule, and ensure that any expirations that
     * are queued up but have not yet run do not run.
     */
    public final void cancel ()
    {
        TimerTask task = _task;
        if (task != null) {
            _task = null;
            task.cancel();
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

        } else {
            // If the task has been defanged, we go ahead and try cancelling
            // it again. The reason for this is that it's possible
            // to have a runaway task if two threads call schedule() and
            // cancel() at the same time.
            // 1) ThreadA calls cancel() and gets a handle on taskA, yields.
            // 2) ThreadB calls schedule(), gets a handle on taskA, cancel()s,
            //    which sets _task to null, then sets up taskB, returns.
            // 3) ThreadA resumes, sets _task to null and re-cancels taskA.
            // taskB is now an active TimerTask but is not referenced anywhere.
            // In case this is taskB, we cancel it so that it doesn't
            // ineffectually expire repeatedly until the JVM exists.
            task.cancel();
        }
    }

    /**
     * The task that schedules actually runs the interval.
     */
    protected class IntervalTask extends TimerTask
    {
        // documentation inherited
        public void run () {
            if (_runQueue == null) {
                safelyExpire(this);
            } else {
                if (_runner == null) { // lazy initialize _runner
                    _runner = new RunQueueRunnable(
                        IntervalTask.this, Interval.this);
                }
                _runQueue.postRunnable(_runner);
            }
        }

        /** If we are using a RunQueue, the Runnable we post to it. */
        protected Runnable _runner;
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
