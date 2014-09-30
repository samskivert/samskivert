//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.HashMap;

import java.util.concurrent.Executor;

import static com.samskivert.util.UtilLog.log;

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
    implements Executor, RunQueue
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
         * long, or 0 to use the default value. If performance tracking is enabled and this unit
         * runs to long, a warning will be logged.
         */
        public long getLongThreshold ()
        {
            return 0L;
        }

        /**
         * Detail specific to this invoker to be included with the warning if this invoker takes
         * longer than the long threshold.  By default, no detail is included.
         */
        public String getDetail ()
        {
            return null;
        }

        /** Returns the name of this invoker. */
        @Override public String toString ()
        {
            return _name;
        }

        protected String _name;
    }

    /**
     * Deprecated, non-functional method that previously set the default long threshold for
     * all invokers. That didn't make sense. Use setLongThreshold() on each invoker.
     */
    @Deprecated
    public static void setDefaultLongThreshold (long millis)
    {
        // no op
    }

    /**
     * Creates an invoker that will post results to the supplied result receiver.
     */
    public Invoker (String name, Executor resultReceiver)
    {
        super(name);
        _receiver = resultReceiver;
    }

    /**
     * Set the long threshold for this Invoker. Units that do not specify their own threshold
     * will be reported as "long" if their duration exceeds this time.
     */
    public void setLongThreshold (long millis)
    {
        _longThreshold = millis;
    }

    /**
     * Posts a unit to this invoker for subsequent invocation on the invoker's thread.
     */
    public void postUnit (Unit unit)
    {
        if (shutdownRequested()) {
            throw new IllegalStateException("Cannot post units to shutdown invoker.");
        }
        // note the time
        unit.queueStamp = System.currentTimeMillis();
        // and append it to the queue
        _queue.append(unit);
    }

    /**
     * Returns the number of units waiting on the queue to be processed. <em>Note:</em> this does
     * not account for whether a unit is <em>currently</em> being processed, so if you want to know
     * definitively, for example that an invoker is empty, it's best to call this method in a unit
     * being processed by that invoker.
     */
    public int getPendingUnits ()
    {
        return _queue.size();
    }

    // from Executor
    public void execute (Runnable command)
    {
        postRunnable(command);
    }

    // from RunQueue
    public void postRunnable (final Runnable r)
    {
        postUnit(new Unit() {
            @Override public boolean invoke () {
                r.run();
                return false;
            }

            @Override public String toString () {
                return "Posted Runnable: " + String.valueOf(r);
            }
        });
    }

    // from RunQueue
    public boolean isDispatchThread ()
    {
        return (this == Thread.currentThread());
    }

    @Override
    public void iterate ()
    {
        // pop the next item off of the queue
        Unit unit = _queue.get();

        long start;
        if (PERF_TRACK) {
            // record the time spent on the queue as a special unit
            start = System.currentTimeMillis();
            // record the time spent on the queue as a special unit
            recordMetrics("queue_wait_time", start - unit.queueStamp);
        } else {
            start = 0L;
        }

        try {
            willInvokeUnit(unit, start);
            if (unit.invoke()) {
                // if it returned true, post it to the receiver thread for result processing
                _receiver.execute(unit);
            }
            didInvokeUnit(unit, start);

        } catch (Throwable t) {
            log.warning("Invocation unit failed", "unit", unit, t);
        }
    }

    /**
     * Shuts down the invoker thread by queueing up a unit that will cause the thread to exit after
     * all currently queued units are processed.
     */
    @Override
    public void shutdown ()
    {
        _shutdownRequested = true;
        _queue.append(new Unit() {
            @Override public boolean invoke () {
                _running = false;
                return false;
            }
        });
    }

    /**
     * Sets the parameters of the unit profiling histogram. This only affects unit lasses that have
     * not yet been run, so should be called early on in program initialization.
     * @param bucketWidthMs size of time buckets, in milliseconds
     * @param bucketCount number of time buckets
     */
    public void setProfilingParameters (int bucketWidthMs, int bucketCount)
    {
        _profileBucketWidth = bucketWidthMs;
        _profileBucketCount = bucketCount;
    }

    /**
     * Returns true if {@link #shutdown} has been called. {@link #isRunning} may still return true
     * until the shutdown unit is reached and processed by the invoker thread.
     */
    protected boolean shutdownRequested ()
    {
        return _shutdownRequested;
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
            long thresh = unit.getLongThreshold();
            if (thresh == 0) {
                thresh = _longThreshold;
            }
            if (duration > thresh) {
                StringBuilder msg = new StringBuilder();
                msg.append((duration >= 10*thresh) ? "Really long" : "Long");
                msg.append(" invoker unit [unit=").append(unit);
                msg.append(" (").append(key).append("), time=").append(duration).append("ms");
                if (unit.getDetail() != null) {
                    msg.append(", detail=").append(unit.getDetail());
                }
                log.warning(msg.append("].").toString());
            }
        }
    }

    protected void recordMetrics (Object key, long duration)
    {
        UnitProfile prof = _tracker.get(key);
        if (prof == null) {
            _tracker.put(key, prof = new UnitProfile(_profileBucketWidth, _profileBucketCount));
        }
        prof.record(duration);
    }

    /** Used to track profile information on invoked units. */
    protected static class UnitProfile
    {
        public UnitProfile (int bucketWidth, int bucketCount) {
            _histo = new Histogram(0, bucketWidth, bucketCount);
        }

        public void record (long duration) {
            _totalElapsed += duration;
            _histo.addValue((int)duration);
        }

        public void clear () {
            _totalElapsed = 0L;
            _histo.clear();
        }

        @Override public String toString () {
            int count = _histo.size();
            return _totalElapsed + "ms/" + count + " = " + (_totalElapsed/count) + "ms avg " +
                StringUtil.toString(_histo.getBuckets());
        }

        protected Histogram _histo;
        protected long _totalElapsed;
    }

    /** The invoker's queue of units to be executed. */
    protected Queue<Unit> _queue = new Queue<Unit>();

    /** The result receiver with which we're working. */
    protected Executor _receiver;

    /** Tracks the counts of invocations by unit's class. */
    protected HashMap<Object,UnitProfile> _tracker = new HashMap<Object,UnitProfile>();

    /** Default size of buckets to use when profiling unit times. */
    protected int _profileBucketWidth = 50;

    /** Default number of buckets to use when profiling unit times. */
    protected int _profileBucketCount = 10;

    /** The long threshold for this particular invoker. */
    protected long _longThreshold = 500L;

    /** True after {@link #shutdown} has been called but before the invoker has finished processing
     * any remaining queued units. */
    protected volatile boolean _shutdownRequested;

    /** Whether or not to track invoker unit performance. */
    protected static final boolean PERF_TRACK = true;
}
