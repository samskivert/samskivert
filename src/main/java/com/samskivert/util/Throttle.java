//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * A throttle is used to prevent code from attempting a particular operation too often. Often it is
 * desirable to retry an operation under failure conditions, but simplistic approaches to retrying
 * operations can lead to large numbers of spurious attempts to do something that will obviously
 * fail. The throttle class provides a mechanism for limiting such attempts by measuring whether or
 * not an activity has been performed N times in the last M seconds. The user of the class decides
 * the appropriate throttle parameters and then simply calls through to throttle to determine
 * whether or not to go ahead with the operation.
 *
 * <p> For example:
 *
 * <pre>
 * protected Throttle _throttle = new Throttle(5, 60000L);
 *
 * public void performOp ()
 *     throws UnavailableException
 * {
 *     if (_throttle.throttleOp()) {
 *         throw new UnavailableException();
 *     }
 *
 *     // perform operation
 * }
 * </pre>
 */
public class Throttle
{
    /**
     * Constructs a new throttle instance that will allow the specified number of operations to
     * proceed within the specified period (the period is measured in milliseconds).
     *
     * <p> As operations and period define a ratio, use the smallest value possible for
     * <code>operations</code> as an array is created to track the time at which each operation was
     * performed (e.g. use 6 ops per 10 seconds rather than 60 ops per 100 seconds if
     * possible). However, note that you may not always want to reduce the ratio as much as
     * possible if you wish to allow bursts of operations up to some large value.
     */
    public Throttle (int operations, long period)
    {
        _ops = new long[operations];
        _period = period;
    }

    /**
     * Updates the number of operations for this throttle to a new maximum, retaining the current
     * history of operations if the limit is being increased and truncating the oldest operations
     * if the limit is decreased.
     *
     * @param operations the new maximum number of operations.
     * @param period the new period.
     */
    public void reinit (int operations, long period)
    {
        _period = period;
        if (operations != _ops.length) {
            long[] ops = new long[operations];

            if (operations > _ops.length) {
                // copy to a larger buffer, leaving zeroes at the beginning
                int lastOp = _lastOp + operations - _ops.length;
                System.arraycopy(_ops, 0, ops, 0, _lastOp);
                System.arraycopy(_ops, _lastOp, ops, lastOp, _ops.length - _lastOp);

            } else {
                // if we're truncating, copy the first (oldest) stamps into ops[0..]
                int endCount = Math.min(operations, _ops.length - _lastOp);
                System.arraycopy(_ops, _lastOp, ops, 0, endCount);
                System.arraycopy(_ops, 0, ops, endCount, operations - endCount);
                _lastOp = 0;
            }
            _ops = ops;
        }
    }

    /**
     * Registers an attempt at an operation and returns true if the operation should be throttled
     * (meaning N operations have already been performed in the last M seconds), or false if the
     * operation is allowed to be performed.
     *
     * @return true if the throttle is activated, false if the operation can proceed.
     */
    public boolean throttleOp ()
    {
        return throttleOp(System.currentTimeMillis());
    }

    /**
     * Registers an attempt at an operation and returns true if the operation should be performed
     * or false if it should be throttled (meaning N operations have already been performed in the
     * last M seconds). For systems that don't wish to use {@link System#currentTimeMillis} (opting
     * in favor for some custom timing mechanism that is more accurate that {@link
     * System#currentTimeMillis}) or those that already have the time, they can avoid an
     * unnecessary call to {@link System#currentTimeMillis} by using this version of the method.
     *
     * @param timeStamp the timestamp at which this operation is being attempted.
     *
     * @return true if the throttle is activated, false if the operation can proceed.
     */
    public boolean throttleOp (long timeStamp)
    {
        if (wouldThrottle(timeStamp)) {
            return true;
        }

        noteOp(timeStamp);
        return false;
    }

    /**
     * Check to see if we would throttle an operation occuring at the specified timestamp.
     * Typically used in conjunction with {@link #noteOp}.
     */
    public boolean wouldThrottle (long timeStamp)
    {
        // if the oldest operation was performed less than _period ago, we need to throttle
        long elapsed = timeStamp - _ops[_lastOp];
        // if negative time elapsed, we must be running on windows; let's just cope by not
        // throttling
        return (elapsed >= 0 && elapsed < _period);
    }

    /**
     * Note that an operation occurred at the specified timestamp. This method should be used with
     * {@link #wouldThrottle} to note an operation that has already been cleared to
     * occur. Typically this is used if there is another limiting factor besides the throttle that
     * determines whether the operation can occur. You are responsible for calling this method in a
     * safe and timely manner after using wouldThrottle.
     */
    public void noteOp (long timeStamp)
    {
        // overwrite the oldest operation with the current time and move the oldest operation
        // pointer to the second oldest operation (which is now the oldest as we overwrote the
        // oldest)
        _ops[_lastOp] = timeStamp;
        _lastOp = (_lastOp + 1) % _ops.length;
    }

    /**
     * Returns the timestamp of the most recently recorded operation.
     */
    public long getLatestOperation ()
    {
        return _ops[(_lastOp + _ops.length - 1) % _ops.length];
    }

    @Override // from Object
    public String toString ()
    {
        long oldest = System.currentTimeMillis() - _ops[_lastOp];
        return _ops.length + " ops per " + _period + "ms (oldest " + oldest + ")";
    }

    /**
     * Used for testing.
     */
    public static void main (String[] args)
    {
        // set up a throttle for 5 ops per 10 seconds
        Throttle throttle = new Throttle(5, 10000);

        // try doing one operation per second and we should hit the throttle on the sixth operation
        // and then kick in again on the eleventh, only to stop again on the fifteenth
        for (int i = 0; i < 20; i++) {
            System.out.println((i+1) + ". Throttle: " + throttle.throttleOp());
            // pause for a sec
            try { Thread.sleep(1000L); }
            catch (InterruptedException ie) {}
        }
    }

    protected long[] _ops;
    protected int _lastOp;
    protected long _period;
}
