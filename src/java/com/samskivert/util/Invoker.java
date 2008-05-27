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

import java.util.HashMap;
import java.util.Iterator;

import static com.samskivert.Log.log;

/**
 * The invoker is used to invoke self-contained units of code on an invoking thread. Each invoker
 * is associated with its own thread and that thread is used to invoke all of the units posted to
 * that invoker in the order in which they were posted. The invoker also provides a convenient
 * mechanism for processing the result of an invocation back on the main thread.
 *
 * <p> The invoker is a useful tool for services that need to block and therefore cannot be run on
 * the main thread. For example, an interactive application might provide an invoker on which to
 * run database queries.
 *
 * <p> Bear in mind that each invoker instance runs units on its own thread and care must be taken
 * to ensure that code running on separate invokers properly synchronizes access to shared
 * information. Where possible, complete isolation of the services provided by a particular invoker
 * is desirable.
 */
public class Invoker extends LoopingThread
    implements RunQueue
{
    /**
     * The unit encapsulates a unit of executable code that will be run on the invoker thread. It
     * also provides facilities for additional code to be run on the main thread once the primary
     * code has completed on the invoker thread.
     */
    public static abstract class Unit implements Runnable
    {
        /** The time at which this unit was placed on the queue. */
        public long queueStamp;

        /** The default constructor. */
        public Unit ()
        {
            this("Unknown");
        }

        /** Creates an invoker unit which will report the supplied name in {@link #toString}. */
        public Unit (String name)
        {
            _name = name;
        }

        /**
         * This method is called on the invoker thread and should be used to perform the primary
         * function of the unit. It can return true to cause the {@link #handleResult} method to be
         * subsequently invoked on the dobjmgr thread (generally to allow the results of the
         * invocation to be acted upon back in the context of the regular world) or false to
         * indicate that no further processing should be performed.
         *
         * @return true if the {@link #handleResult} method should be called on the main thread,
         * false if not.
         */
        public abstract boolean invoke ();

        /**
         * Invocation unit implementations can implement this function to perform any post unit
         * invocation processing back on the main thread. It will be invoked if {@link #invoke}
         * returns true.
         */
        public void handleResult ()
        {
            // do nothing by default
        }

        // we want to be a runnable to make the receiver interface simple, but we'd like for
        // invocation unit implementations to be able to put their result handling code into an
        // aptly named method
        public void run ()
        {
            handleResult();
        }

        /**
         * Returns the duration beyond which this invoker unit will be considered to be running too
         * long and result in a warning being logged. The default is 500ms.
         */
        public long getLongThreshold ()
        {
            return _defaultLongThreshold;
        }

        /** Returns the name of this invoker. */
        public String toString ()
        {
            return _name;
        }

        protected String _name;
    }

    /**
     * Configures the default duration (in milliseconds) for an invoker unit to be reported as
     * "long". Long units will result in a warning message written to the log.
     */
    public static void setDefaultLongThreshold (long millis)
    {
        _defaultLongThreshold = millis;
    }

    /**
     * Creates an invoker that will post results to the supplied result receiver.
     */
    public Invoker (String name, RunQueue resultReceiver)
    {
        super(name);
        _receiver = resultReceiver;
    }

    /**
     * Posts a unit to this invoker for subsequent invocation on the invoker's thread.
     */
    public void postUnit (Unit unit)
    {
        if (!isRunning()) {
            throw new IllegalStateException("Invoker has been shutdown");
        }
        // note the time
        unit.queueStamp = System.currentTimeMillis();
        // and append it to the queue
        _queue.append(unit);
    }

    // from RunQueue
    public void postRunnable (final Runnable r)
    {
        postUnit(new Unit() {
            public boolean invoke () {
                r.run();
                return false;
            }

            public String toString () {
                return "Posted Runnable: " + String.valueOf(r);
            }
        });
    }

    // from RunQueue
    public boolean isDispatchThread ()
    {
        return (this == Thread.currentThread());
    }

    // documentation inherited
    public void iterate ()
    {
        // pop the next item off of the queue
        Unit unit = _queue.get();

        long start;
        if (PERF_TRACK) {
            // record the time spent on the queue as a special unit
            start = System.currentTimeMillis();
            synchronized (this) {
                _unitsRun++;
            }
            // record the time spent on the queue as a special unit
            recordMetrics("queue_wait_time", start - unit.queueStamp);
        }

        try {
            willInvokeUnit(unit, start);
            if (unit.invoke()) {
                // if it returned true, post it to the receiver thread for result processing
                _receiver.postRunnable(unit);
            }
            didInvokeUnit(unit, start);

        } catch (Throwable t) {
            log.warning("Invocation unit failed [unit=" + unit + "].", t);
        }
    }

    /**
     * Shuts down the invoker thread by queueing up a unit that will cause the thread to exit after
     * all currently queued units are processed.
     */
    public void shutdown ()
    {
        _queue.append(new Unit() {
            public boolean invoke () {
                _running = false;
                return false;
            }
        });
    }

    /**
     * Called before we process an invoker unit.
     *
     * @param unit the unit about to be invoked.
     * @param start a timestamp recorded immediately before invocation if {@link #PERF_TRACK} is
     * enabled, 0L otherwise.
     */
    protected void willInvokeUnit (Unit unit, long start)
    {
    }

    /**
     * Called before we process an invoker unit.
     *
     * @param unit the unit about to be invoked.
     * @param start a timestamp recorded immediately before invocation if {@link #PERF_TRACK} is
     * enabled, 0L otherwise.
     */
    protected void didInvokeUnit (Unit unit, long start)
    {
        // track some performance metrics
        if (PERF_TRACK) {
            long duration = System.currentTimeMillis() - start;
            Object key = unit.getClass();
            recordMetrics(key, duration);

            // report long runners
            if (duration > unit.getLongThreshold()) {
                String howLong = (duration >= 10*unit.getLongThreshold()) ? "Really long" : "Long";
                log.warning(howLong + " invoker unit [unit=" + unit + " (" + key +
                            "), time=" + duration + "ms].");
            }
        }
    }

    protected void recordMetrics (Object key, long duration)
    {
        UnitProfile prof = _tracker.get(key);
        if (prof == null) {
            _tracker.put(key, prof = new UnitProfile());
        }
        prof.record(duration);
    }

    /** Used to track profile information on invoked units. */
    protected static class UnitProfile
    {
        public void record (long duration) {
            _totalElapsed += duration;
            _histo.addValue((int)duration);
        }

        public void clear () {
            _totalElapsed = 0L;
            _histo.clear();
        }

        public String toString () {
            int count = _histo.size();
            return _totalElapsed + "ms/" + count + " = " + (_totalElapsed/count) + "ms avg " +
                StringUtil.toString(_histo.getBuckets());
        }

        // track in buckets of 50ms up to 500ms
        protected Histogram _histo = new Histogram(0, 50, 10);
        protected long _totalElapsed;
    }

    /** The invoker's queue of units to be executed. */
    protected Queue<Unit> _queue = new Queue<Unit>();

    /** The result receiver with which we're working. */
    protected RunQueue _receiver;

    /** Tracks the counts of invocations by unit's class. */
    protected HashMap<Object,UnitProfile> _tracker = new HashMap<Object,UnitProfile>();

    /** The total number of invoker units run since the last report. */
    protected int _unitsRun;

    /** The duration of time after which we consider a unit to be delinquent and log a warning. */
    protected static long _defaultLongThreshold = 500L;

    /** Whether or not to track invoker unit performance. */
    protected static final boolean PERF_TRACK = true;
}
