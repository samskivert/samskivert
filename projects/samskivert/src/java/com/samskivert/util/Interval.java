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
    implements Runnable
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
    public final void schedule (long initialDelay, long repeatDelay)
    {
        cancel();
        _task = new TimerTask() {
            public void run () {
                if (this != _task) {
                    return;
                }

                // if the Interval is operating in simple mode,
                // just expire here
                if (_runQueue == null) {
                    safelyExpire();
                    return;
                }

                // else we need to make sure we want to run
                // and post the Runnable
                synchronized (this) {
                    if (_fired != -1) {
                        _fired++;
                        _runQueue.postRunnable(Interval.this);
                    }
                }
            }
        };

        synchronized (_task) {
            _fired = _expired = 0;
            if (repeatDelay == 0L) {
                _timer.schedule(_task, initialDelay);
            } else {
                _timer.scheduleAtFixedRate(_task, initialDelay, repeatDelay);
            }
        }
    }

    /**
     * Cancel the Interval, and ensure that any expirations that are queued
     * up but have not yet run do not run.
     */
    public final void cancel ()
    {
        if (_task != null) { // task can only be null if we were never scheduled
            synchronized (_task) {
                _task.cancel();
                _fired = -1;
            }
        }
    }

    // documentation inherited from interface Runnable
    public final void run ()
    {
        if (_expired >= _fired) {
            // we are a dead interval. We were queued up prior to cancel()
            // being called, but we are now running after cancel()
            return;
        }

        // increment expired and scoot everything back if we're getting too big
        _expired++;
        if (_expired > Integer.MAX_VALUE/2) {
            synchronized (_task) {
                _expired -= Integer.MAX_VALUE/2;
                _fired -= Integer.MAX_VALUE/2;
            }
        }

        safelyExpire();
    }

    /**
     * Safely expire the interval.
     */
    protected final void safelyExpire ()
    {
        try {
            expired();
        } catch (Throwable t) {
            Log.warning("Interval broken in expired(): " + t);
            Log.logStackTrace(t);
        }
    }

    /** Counters used to guarantee that we don't fuck up. */
    protected int _fired = -1, _expired = 0;

    /** If non-null, the RunQueue used to run the expired() method for each
     * Interval. */
    protected RunQueue _runQueue;

    /** The task that actually schedules our execution with the static Timer.
     * Also the object that we synchronize upon when dealing with those issues.
     */
    protected TimerTask _task;

    /** The daemon timer used to schedule all Intervals. */
    protected static final Timer _timer =
        new Timer(/*JDK1.5 "samskivert Interval Timer",*/ true);
}
