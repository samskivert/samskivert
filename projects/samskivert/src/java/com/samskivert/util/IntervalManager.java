//
// $Id: IntervalManager.java,v 1.10 2003/08/13 21:04:12 mdb Exp $
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
 * Can be used to register an object that is to be called once or
 * repeatedly after an interval has expired. This now uses {@link Timer}
 * to do all the actual scheduling, but we keep this front end because it
 * is static (accessible anywhere), provides a single thread where
 * disparate simple intervals can all run happily, and because {@link
 * Interval} is an interface, unlike {@link TimerTask}, which is an
 * abstract class.
 */
public class IntervalManager
{
    /**
     * Schedules the intervaled object to get called after an interval.
     *
     * @param i the intervaled object
     * @param delay the milliseconds until the interval expires.
     * @param arg the object to be passed to the interval when the
     * interval delay expires.
     * @param recur if true, interval gets called every timeout until
     * removed.
     *
     * @return an ID number that will passed to the {@link
     * Interval#intervalExpired} method of i along with arg
     */
    public static int register (
        Interval i, long delay, Object arg, boolean recur)
    {
        IntervalTask task = new IntervalTask(i, arg, recur);
        _hash.put(task.id, task);

        if (recur) {
            _timer.scheduleAtFixedRate(task, delay, delay);
        } else {
            _timer.schedule(task, delay);
        }

        return task.id;
    }

    /**
     * Schedule the interval to be called repeatedly, but at a fixed delay
     * instead of fixed rate. This means that the delay between executions
     * will be the delay specified below plus the actual execution time it
     * takes to run the interval's expire method.
     */
    public static int registerAtFixedDelay (Interval i, long delay, Object arg)
    {
        IntervalTask task = new IntervalTask(i, arg, true);
        _hash.put(task.id, task);
        _timer.schedule(task, delay, delay);
        return task.id;
    }

    /**
     * Non-recurring intervals are removed automatically after they are
     * run! This method is only useful if you want to remove a recurring
     * {@link Interval} or if you want to remove a non-recurring {@link
     * Interval} before it gets run.
     */
    public static void remove (int id)
    {
        IntervalTask task = (IntervalTask) _hash.remove(id);
        if (task != null) {
            task.cancel();
        } else {
            Log.warning("remove() called on non-registered " +
                        "interval [id=" + id + "].");
        }
    }

    /**
     * Turn on or off logging of all interval firings.
     */
    public static void setLogIntervals (boolean log)
    {
        _logging = log;
    }

    /**
     * Returns the total number of intervals registered.
     */
    public static int registeredIntervalCount ()
    {
        return _hash.size();
    }

    /**
     * Returns the number of intervals fired since the last call to this
     * method.
     */
    public static int getAndClearFiredIntervals ()
    {
        int fired;
        synchronized (_hash) {
            fired = _firedIntervals;
            _firedIntervals = 0;
        }
        return fired;
    }

    /** The timer we use to schedule everything. */
    protected static Timer _timer = new Timer(true);

    /** Our registered intervals, indexed by id. */
    protected static IntMap _hash =
        Collections.synchronizedIntMap(new HashIntMap());

    /** Whether or not we're logging interval fires. */
    protected static boolean _logging = false;

    /** Used by {@link #getAndClearFiredIntervals}. */
    protected static int _firedIntervals;

    /**
     * A class to adapt {@link TimerTask} to the smooth action of {@link
     * Interval}.
     */
    static class IntervalTask extends TimerTask
    {
        public int id;
        protected Interval _i;
        protected Object _arg;
        protected boolean _onetime;
        protected String _source;

        public IntervalTask (Interval i, Object arg, boolean recur)
        {
            _i = i;
            _arg = arg;
            _onetime = !recur;
            id = nextID();
            try {
                _source = new Throwable().getStackTrace()[2].toString();
            } catch (Throwable oopsie) {
                _source = "<unknown>";
            }
        }

        /**
         * Run the interval. It's synchronized so that if we someday have
         * multiple threads, the same interval won't be called multiple
         * times.
         */
        public void run ()
        {
            if (_logging) {
                System.err.println("Interval fired [source=" + _source + "]");
            }

            // note that we fired another interval
            synchronized (_hash) {
                _firedIntervals++;
            }

            // protect our ass.
            try {
                _i.intervalExpired(id, _arg);
            } catch (Exception e) {
                Log.warning("Exception while expiring interval: " + e);
                Log.logStackTrace(e);
            }

            // if we're not recurring, be sure to remove from the mgr's hash 
            if (_onetime) {
                _hash.remove(id);
            }
        }

        /**
         * Unique ids for each interval item.
         */
        private static synchronized int nextID ()
        {
            return _idseq++;
        }

        private static int _idseq = 0;
    }
}
