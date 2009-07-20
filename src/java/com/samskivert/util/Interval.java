//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.samskivert.Log.log;

/**
 * An interface for doing operations after some delay. Allows expiration to occur on a specific
 * thread, and guarantees that any queued expiration will not run if the Interval has since been
 * cancelled or rescheduled.
 */
public abstract class Interval
{
    /**
     * An interface that will be implemented by the runnable posted to a RunQueue that can be used
     * to retrieve the original Interval.
     */
    public static interface RunBuddy extends Runnable
    {
        /**
         * Retrieve the Interval that is responsible for posting this RunBuddy to a RunQueue. Most
         * likely used to call toString() on the Interval for logging purposes.
         */
        public Interval getInterval ();
    }

    /**
     * Creates an interval that executes the supplied runnable when it expires.
     */
    public static Interval create (final Runnable onExpired)
    {
        return new Interval() {
            public void expired () {
                onExpired.run();
            }
        };
    }

    /**
     * Creates an interval that executes the supplied runnable on the specified RunQueue when it
     * expires.
     */
    public static Interval create (RunQueue runQueue, final Runnable onExpired)
    {
        // we could probably avoid all the wacky machinations internal to Interval that do the
        // runbuddy reposting and whatever and just create a non-runqueue interval that posts the
        // supplied runnable to the runqueue when it expires, but we'll just punt on that for now
        return new Interval(runQueue) {
            public void expired () {
                onExpired.run();
            }
        };
    }

    /**
     * This may be removed.
     *
     * @deprecated
     */
    @Deprecated
    public static void resetTimer ()
    {
    }

    /**
     * Create a simple interval that does not use a RunQueue to run the {@link #expired} method.
     */
    public Interval ()
    {
        // _runQueue stays null
    }

    /**
     * Create an Interval that uses the specified {@link RunQueue} to run the {@link #expired}
     * method. If null is supplied the interval will be run directly on the timer thread.
     */
    public Interval (RunQueue runQueue)
    {
        setRunQueue(runQueue);
    }

    /**
     * Configures the run queue to be used by this interval. This <em>must</em> be called before
     * the interval is started and a non-null queue must be provided. This exists for situations
     * where the caller needs to configure an optional run queue and thus can't easily call the
     * appropriate constructor.
     */
    public void setRunQueue (RunQueue runQueue)
    {
        if (runQueue == null) {
            throw new IllegalArgumentException("Supplied RunQueue must be non-null");
        }
        _runQueue = runQueue;
    }

    /**
     * The main method where your interval should do its work.
     */
    public abstract void expired ();

    /**
     * Schedules this interval to execute once at <code>when</code>. Supersedes any previous
     * schedule that this Interval may have had.
     *
     * @return this for convenient method chaining.
     */
    public final Interval schedule (Date when)
    {
        return schedule(when.getTime() - System.currentTimeMillis());
    }

    /**
     * Schedule the interval to execute once, after the specified delay.  Supersedes any previous
     * schedule that this Interval may have had.
     *
     * @return this for convenient method chaining.
     */
    public final Interval schedule (long delay)
    {
        return schedule(delay, 0L);
    }

    /**
     * Schedule the interval to execute repeatedly with fixed-rate scheduling between repeats,
     * with the same delay. Supersedes any previous schedule that this Interval may have had.
     *
     * <p> Note: if a repeating interval is scheduled to post itself to a RunQueue and the target
     * RunQueue is shutdown when the interval expires, the interval will cancel itself and log a
     * warning message. </p>
     *
     * @return this for convenient method chaining.
     */
    public final Interval schedule (long delay, boolean repeat)
    {
        return schedule(delay, repeat ? delay : 0L);
    }

    /**
     * Schedule the interval to execute repeatedly with the specified initial delay and repeat
     * delay with fixed-rate scheduling between repeats. Supersedes any previous schedule that this
     * Interval may have had.
     *
     * <p> Note: if a repeating interval is scheduled to post itself to a RunQueue and the target
     * RunQueue is shutdown when the interval expires, the interval will cancel itself and log a
     * warning message. </p>
     *
     * @return this for convenient method chaining.
     */
    public final Interval schedule (long initialDelay, long repeatDelay)
    {
        return schedule(initialDelay, repeatDelay, true);
    }

    /**
     * Schedule the interval to execute repeatedly with the specified initial delay and repeat
     * delay. Supersedes any previous schedule that this Interval may have had.
     *
     * <p> Note: if a repeating interval is scheduled to post itself to a RunQueue and the target
     * RunQueue is shutdown when the interval expires, the interval will cancel itself and log a
     * warning message. </p>
     *
     * @param fixedRate - if true, this interval schedules repeated expirations using
     * {@link Timer#scheduleAtFixedRate(TimerTask, long, long)} ensuring that the number of
     * expired calls will match the amount of time elapsed. If false, it uses
     * {@link Timer#schedule(TimerTask, long, long)} which ensures that there will be close to
     * <code>repeateDelay</code> milliseconds between expirations.
     *
     * @return this for convenient method chaining.
     *
     * @exception IllegalArgumentException if fixedRate is false and a RunQueue has been specified.
     * That doesn't make sense because the fixed delay cannot account for the time that the
     * RunBuddy sits on the RunQueue waiting to call expire().
     */
    public final Interval schedule (long initialDelay, long repeatDelay, boolean fixedRate)
    {
        cancel();
        IntervalTask task = new IntervalTask(this);
        _task = task;

        // try twice to schedule the task- see comment inside the catch
        try {
            scheduleTask(initialDelay, repeatDelay, fixedRate);

        } catch (IllegalStateException ise) {
            // Timer.schedule will only throw this if the TimerThead was shut down.
            // This may happen automatically in Applets, so we need to create a new
            // Timer now. Note that in a multithreaded environment it may be possible
            // to have more than one Timer after this happens. That would be slightly
            // undesirable but should not break anything.
            _timer = createTimer();
            scheduleTask(initialDelay, repeatDelay, fixedRate);
        }

        return this;
    }

    /**
     * Cancel the current schedule, and ensure that any expirations that are queued up but have not
     * yet run do not run.
     */
    public final void cancel ()
    {
        IntervalTask task = _task;
        if (task != null) {
            _task = null;
            task.cancel();
        }
    }

    protected final void scheduleTask (long initialDelay, long repeatDelay, boolean fixedRate)
    {
        if (repeatDelay == 0L) {
            _timer.schedule(_task, initialDelay);
        } else if (fixedRate) {
            _timer.scheduleAtFixedRate(_task, initialDelay, repeatDelay);
        } else if (_runQueue != null) {
            throw new IllegalArgumentException(
                "Cannot schedule at a fixed delay when using a RunQueue.");
        } else {
            _timer.schedule(_task, initialDelay, repeatDelay);
        }
    }

    protected final void safelyExpire (IntervalTask task)
    {
        // only expire the interval if the task is still valid
        if (_task == task) {
            try {
                expired();
            } catch (Throwable t) {
                log.warning("Interval broken in expired() " + this, t);
            }

        } else {
            // If the task has been defanged, we go ahead and try cancelling it again. The reason
            // for this is that it's possible to have a runaway task if two threads call schedule()
            // and cancel() at the same time.
            // 1) ThreadA calls cancel() and gets a handle on taskA, yields.
            // 2) ThreadB calls schedule(), gets a handle on taskA, cancel()s, which sets _task to
            //    null, then sets up taskB, returns.
            // 3) ThreadA resumes, sets _task to null and re-cancels taskA.  taskB is now an active
            //    TimerTask but is not referenced anywhere.  In case this is taskB, we cancel it so
            //    that it doesn't ineffectually expire repeatedly until the JVM exists.
            task.cancel();
        }
    }

    protected static Timer createTimer ()
    {
        return new Timer(/*JDK1.5 "samskivert Interval Timer",*/ true);
    }

    /**
     * The task that schedules actually runs the interval.
     */
    protected static class IntervalTask extends TimerTask
    {
        public IntervalTask (Interval interval)
        {
            _interval = interval;
        }

        @Override public boolean cancel ()
        {
            // remove the reference back to the interval, allowing the Interval itself
            // to be gc'd even as this Task potentially sits on the Timer queue.
            _interval = null;
            return super.cancel();
        }

        @Override public void run () {
            Interval ival = _interval;
            if (ival == null) {
                return;
            }
            if (ival._runQueue == null) {
                ival.safelyExpire(this);
                return;
            }

            if (_runner == null) { // lazy initialize _runner
                _runner = new RunBuddy() {
                    public void run () {
                        Interval ival = _interval;
                        if (ival != null) {
                            ival.safelyExpire(IntervalTask.this);
                        }
                    }
                    public Interval getInterval () {
                        return _interval;
                    }
                    @Override public String toString () {
                        Interval ival = _interval;
                        return (ival != null) ? ival.toString() : "(Interval was cancelled)";
                    }
                };
            }

            if (ival._runQueue.isRunning()) {
                try {
                    ival._runQueue.postRunnable(_runner);
                } catch (Exception e) {
                    log.warning("Failed to execute interval on run-queue", "queue", ival._runQueue,
                                "interval", ival, e);
                }

            } else {
                log.warning("Interval posted to shutdown RunQueue. Cancelling.",
                            "queue", ival._runQueue, "interval", ival);
                ival.cancel();
            }
        }

        /** If we are using a RunQueue, the Runnable we post to it. */
        protected RunBuddy _runner;

        /** The interval this task is for. We have this reference back to our interval rather
         * than just being a non-static inner class because when a TimerTask is cancelled
         * it still sits on the Timer's queue until its execution time is reached. We want
         * any references held by the interval to be collectable during this period, so our
         * cancel removes the reference back to the Interval. */
        protected Interval _interval;

    } // end: static class IntervalTask

    /** If non-null, the RunQueue used to run the expired() method for each Interval. */
    protected RunQueue _runQueue;

    /** The task that actually schedules our execution with the static Timer. */
    protected volatile IntervalTask _task;

    /** The daemon timer used to schedule all intervals. */
    protected static Timer _timer = createTimer();
}
