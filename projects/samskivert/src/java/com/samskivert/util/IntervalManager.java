//
// $Id: IntervalManager.java,v 1.5 2002/02/19 03:39:41 mdb Exp $
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

import com.samskivert.Log;

/**
 * Interval: can be used to register an object that is to be called once
 * or repeatedly after an interval has expired.
 *
 * <p> A caveat: be careful about deadlocks kids. The number of helper
 * threads should exceed the number of times you nest calls to Interval
 * stuff from inside the intervalExpired method of an Interval.  Normally,
 * if all you're using this for is interrupting other threads or calling
 * repaint you can get by with 0 helpers. If you want to do some more
 * complicated stuff in intervalExpired() then you probably want a few
 * threads in the pool. If you're doing extensive processing and setting
 * up other timeouts on that thread then you want to make sure you've got
 * plenty of threads.
 */
public class IntervalManager extends Thread
{
    /**
     * Sets the maximum number of helper threads to run methods in
     * Intervals.  Defaults to 0, meaning that the main thread does all
     * the work.  Setting this to a nonzero value makes it such that a
     * pool of helper threads is created to do all the actual work.
     */
    public static synchronized void setMaxHelperThreads (int newmax)
    {
	newmax = Math.max(newmax, 0);

	// if we are moving down, we need to kill some threads.
	if (_helpers > newmax) {
	    for (int ii=0; ii < _helpers - newmax; ii++) {
		_queue.append(KILLHELPER);
	    }
	    _helpers = newmax;
	}

	// finally, set the new maximum thread pool size.
	_maxhelpers = newmax;
    }

    /**
     * Increment the number of maximum helper threads.  This is useful if
     * you may have many packages which use the IntervalMgr as a
     * threadpool to do work, each package can statically initialize the
     * number of threads they want to be available by calling this method
     * and they'll all be added together.
     */
    public static synchronized void incrementHelperThreads (int amt)
    {
	setMaxHelperThreads(_maxhelpers + amt);
    }

    /**
     * Schedule the intervaled object to get called after an interval.
     *
     * @param i the intervaled object
     * @param timout # of ms until interval is up.
     * @param arg object to be passed when interval expires
     * @param recur if true, interval gets called every timeout until
     *              removed.
     *
     * @return an ID number that will passed to the intervalExpired method
     * of i along with arg
     */
    public static int register (Interval i, long delay, Object arg,
				boolean recur)
    {
	synchronized (_mgr) {
	    IntervalItem item = new IntervalItem(i, delay, arg, recur);
	    _hash.put(item.id, item);
	    _schedule.add(item);
            _schedule.sort();
	    if (item.endtime < _nextwake) {
		_mgr.notify();
	    }
	    return item.id;
	}
    }

    /**
     * Non-recurring intervals are removed automatically after they are
     * run!  This method is only useful if you want to remove a recurring
     * Interval or if you want to remove a non-recurring Interval before
     * it gets run.
     */
    public static void remove (int id)
    {
	synchronized (_mgr) {
	    IntervalItem item = (IntervalItem) _hash.remove(id);
	    if (item != null) {
		_schedule.remove(item);
		return;
	    }
	}

	Log.warning("remove() called on non-registered " +
		    "interval [id=" + id + "].");
	Thread.dumpStack();
    }

    /**
     * Do our interval thing.
     */
    public void run ()
    {
	while (_mgr == Thread.currentThread()) {

	    // check to see if an interval has expired, if so call
	    // expired from an unsynchronized position.
//  	    TrackedThread.setState("Checking intervals");
	    IntervalItem item = checkInterval();
	    if (item != null) {
		if (_maxhelpers > 0) {
		    helperHandle(item);
		} else {
		    item.expired();  // we have no helpers, do it ourselves.
		}
	    }

	    // now attempt to sleep
//  	    TrackedThread.setState("Waiting for Interval event...");
	    doWait();
	}
    }

    /**
     * Have a helper thread handle the expiration of this interval.
     */
    protected static synchronized void helperHandle (IntervalItem item)
    {
	// put the item on the queue...
	_busyhelpers++;
	_queue.append(item);

	// possibly create a new thread in the pool to do this work.
	if ((_busyhelpers > _helpers) && (_helpers < _maxhelpers)) {
	    IntervalExpirer helper = new IntervalExpirer(_queue);
	    _helpers++;
	    helper.start();
	}
    }

    /**
     * A helper thread lets us know when it has finished its work.
     */
    protected static synchronized void helperFinished ()
    {
	_busyhelpers--;
    }

    /**
     * Check to see if anything needs calling.
     */
    private synchronized IntervalItem checkInterval ()
    {
//  	TrackedThread.setState("Checking intervals");
	if (_schedule.size() > 0) {
	    IntervalItem item = (IntervalItem) _schedule.get(0);

	    // it's totally valid for us to wake up early..
	    // so make sure we really want to run the first item on the queue.
	    if (item.endtime <= System.currentTimeMillis()) {
		// we gotta deal with this monster!
		// first, remove it.
		_schedule.remove(0);
		if (item.checkRecur()) {
		    // since we're definitionally not sleeping, we can just
		    // insert into the queue without checking wait times..
		    _schedule.add(item);
                    _schedule.sort();

		} else {
		    // otherwise, get rid of this interval altogether
		    _hash.remove(item.id);
		}

		return item;
	    }
	}

	return null;
    }

    /**
     * Sleep until the next Interval needs to be attended to.
     */
    private synchronized void doWait ()
    {
//  	TrackedThread.setState("Waiting for Interval event...");
	if (_schedule.size() == 0) {
	    _nextwake = Long.MAX_VALUE;
	} else {
	    IntervalItem item = (IntervalItem) _schedule.get(0);
	    _nextwake = item.endtime;
	}

	long waittime = _nextwake - System.currentTimeMillis();
	if (waittime > 0L) {
	    try {
		wait(waittime);
	    } catch (InterruptedException e) {
	    }
	}
    }

    private IntervalManager ()
    {
	super("IntervalManager");
	setDaemon(true);
	start();
    }

    // there can be only one!
    protected static IntervalManager _mgr = new IntervalManager();

    // We just use a sorted list for now since we aren't likely to have
    // too many monitorables at any time. Insertions are O(log n),
    // removals are O(n) since we search then entire queue to find the
    // intervaleds to remove.
    // 
    // If we someday find that we have a lot of intervaleds, we may want
    // to rewrite this such that we can add and remove intervaleds much
    // faster.
    protected static SortableArrayList _schedule = new SortableArrayList();

    protected static HashIntMap _hash = new HashIntMap();
    protected static long _nextwake = Long.MAX_VALUE;

    protected static int _helpers = 0;       // # of created helpers
    protected static int _maxhelpers = 0;    // max # of helpers we can create
    protected static int _busyhelpers = 0;   // # of outstanding requests
    protected static Queue _queue = new Queue();

    protected static final Object KILLHELPER = new Object();
}

class IntervalItem implements Comparable
{
    public long endtime;
    public int id;

    protected long _timeout;
    protected boolean _recur;
    protected Interval _i;
    protected Object _arg;

    public IntervalItem (Interval i, long timeout, Object arg, boolean recur)
    {
	_i = i;
	_timeout = Math.max(timeout, 0);
	_arg = arg;
	_recur = recur;

	id = nextID();
	endtime = System.currentTimeMillis() + timeout;
    }

    public int compareTo (Object other)
    {
	return (int) (endtime - ((IntervalItem) other).endtime);
    }

    /**
     * Test-n-set recurring stuff. If we do recur, increment the time we
     * are to next wake.
     */
    public boolean checkRecur ()
    {
	if (_recur) {
	    endtime += _timeout;
	}
	return _recur;
    }

    /**
     * Run the interval. It's synchronized so that if we someday have
     * multiple threads, the same interval won't be called multiple times.
     */
    public synchronized void expired ()
    {
	// protect our ass.
	try {
	    _i.intervalExpired(id, _arg);
	} catch (Exception e) {
	    Log.warning("Exception while expiring interval: " + e);
	    Log.logStackTrace(e);
	}
    }

    /**
     * For debugging.
     */
    public String toString()
    {
	return "Interval: " + _i + " (wakes in " +
		(endtime - System.currentTimeMillis()) + "ms)" +
		(_recur ? (" [recurring every " + _timeout + "ms]") : "");
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

/**
 * These are the helper threads for the IntervalManager.
 */
class IntervalExpirer extends Thread
{
    public IntervalExpirer (Queue queue)
    {
	super("IntervalExpirer");
	setDaemon(true);
	_queue = queue;
    }

    public void run ()
    {
	while (true) {
//  	    TrackedThread.setState("Waiting for Interval to run...");
	    Object o = _queue.get();
	    if (o == IntervalManager.KILLHELPER) {
		break; //exit
	    }

	    // otherwise run the bashtard!
	    ((IntervalItem) o).expired();

	    IntervalManager.helperFinished();
	}
    }

    protected Queue _queue;
}
