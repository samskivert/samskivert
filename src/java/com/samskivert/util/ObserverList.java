//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.samskivert.Log.log;

/**
 * Provides a simplified mechanism for maintaining a list of observers (or listeners or whatever
 * you like to call them) and notifying those observers when desired. Notification takes place
 * through a {@link ObserverOp} which allows the list maintainer to call an arbitrary method on its
 * observers.
 *
 * <p> A couple of different usage patterns will help to illuminate. The first, where a new notify
 * operation is created every time the observers are notified. This wins on brevity but loses on
 * performance.
 *
 * <pre>
 * ...
 *     // notify our observers
 *     final int foo = 19;
 *     final String bar = "yay!";
 *     _observers.apply(new ObserverList.ObserverOp() {
 *         public boolean apply (Object observer) {
 *             ((MyHappyObserver)observer).foozle(foo, bar);
 *             return true;
 *         }
 *     });
 * ...
 * </pre>
 *
 * The second where a singleton instance of the operation class is maintained and is configured
 * each time notification is desired.
 *
 * <pre>
 * ...
 *     protected static class FoozleOp implements ObserverOp
 *     {
 *         public void init (int foo, String bar)
 *         {
 *             _foo = foo;
 *             _bar = bar;
 *         }
 *
 *         public boolean apply (Object observer)
 *         {
 *             ((MyHappyObserver)observer).foozle(_foo, _bar);
 *             return true;
 *         }
 *
 *         protected int _foo;
 *         protected String _bar;
 *     }
 * ...
 *     // notify our observers
 *     _foozleOp.init(19, "yay!");
 *     _observers.apply(_foozleOp);
 * ...
 *     protected static FoozleOp _foozleOp = new FoozleOp();
 * ...
 * </pre>
 *
 * Bear in mind that this latter case is not thread safe.
 *
 * <p> Other usage patterns are most certainly conceivable, and hopefully these two will give you a
 * useful starting point for determining what is the most appropriate usage for your needs.
 */
public class ObserverList<T> extends ArrayList<T>
{
    /**
     * Instances of this interface are used to apply methods to all observers in a list.
     */
    public static interface ObserverOp<T>
    {
        /**
         * Called once for each observer in the list.
         *
         * @return true if the observer should remain in the list, false if it should be removed in
         * response to this application.
         */
        public boolean apply (T observer);
    }

    /** A notification ordering policy indicating that the observers should be notified in the
     * order they were added and that the notification should be done on a snapshot of the
     * array. */
    public static final int SAFE_IN_ORDER_NOTIFY = 1;

    /** A notification ordering policy wherein the observers are notified last to first so that
     * they can be removed during the notification process and new observers added will not
     * inadvertently be notified as well, but no copy of the observer list need be made. This will
     * not work if observers are added or removed from arbitrary positions in the list during a
     * notification call. */
    public static final int FAST_UNSAFE_NOTIFY = 2;

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> ObserverList<T> newSafeInOrder ()
    {
        return new ObserverList<T>(SAFE_IN_ORDER_NOTIFY);
    }

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> ObserverList<T> newFastUnsafe ()
    {
        return new ObserverList<T>(FAST_UNSAFE_NOTIFY);
    }

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> ObserverList<T> newList (int notifyPolicy, boolean allowDups)
    {
        return new ObserverList<T>(notifyPolicy, allowDups);
    }

    /**
     * Creates an empty observer list with the supplied notification policy and that only allows
     * observers to observe the list once.
     *
     * @param notifyPolicy Either {@link #SAFE_IN_ORDER_NOTIFY} or {@link #FAST_UNSAFE_NOTIFY}.
     */
    public ObserverList (int notifyPolicy)
    {
        this(notifyPolicy, false);
    }

    /**
     * Creates an empty observer list with the supplied notification and duplicate observer policy.
     *
     * @param notifyPolicy Either {@link #SAFE_IN_ORDER_NOTIFY} or {@link #FAST_UNSAFE_NOTIFY}.
     * @param allowDups whether to allow observers to be added to the list more than once.
     */
    public ObserverList (int notifyPolicy, boolean allowDups)
    {
        // make sure the policy is valid
        if (notifyPolicy != SAFE_IN_ORDER_NOTIFY && notifyPolicy != FAST_UNSAFE_NOTIFY) {
            throw new RuntimeException("Invalid notification policy [policy=" + notifyPolicy + "]");
        }

        _policy = notifyPolicy ;
        _allowDups = allowDups;
    }

    @Override
    public void add (int index, T element)
    {
        if (element == null) {
            throw new NullPointerException("Null observers not allowed.");
        }
        if (!isDuplicate(element)) {
            super.add(index, element);
        }
    }

    @Override
    public boolean add (T element)
    {
        if (element == null) {
            throw new NullPointerException("Null observers not allowed.");
        }
        return isDuplicate(element) ? false : super.add(element);
    }

    @Override
    public boolean addAll (Collection<? extends T> c)
    {
        throw new UnsupportedOperationException(
            "Observers may only be added via ObserverList.add(Object).");
    }

    @Override
    public boolean addAll (int index, Collection<? extends T> c)
    {
        throw new UnsupportedOperationException(
            "Observers may only be added via ObserverList.add(Object).");
    }

    @Override
    public int indexOf (Object element)
    {
        // indexOf and lastIndexOf are implemented using reference equality so that contains() also
        // uses reference equality
        for (int ii = 0, nn = size(); ii < nn; ii++) {
            if (element == get(ii)) {
                return ii;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf (Object element)
    {
        // indexOf and lastIndexOf are implemented using reference equality so that contains() also
        // uses reference equality
        for (int ii = size() - 1; ii >= 0; ii--) {
            if (element == get(ii)) {
                return ii;
            }
        }
        return -1;
    }

    @Override
    public boolean remove (Object element)
    {
        int dex = indexOf(element);
        if (dex == -1) {
            return false; // not found
        }
        remove(dex);
        return true;
    }

    /**
     * Applies the supplied observer operation to all observers in the list in a manner conforming
     * to the notification ordering policy specified at construct time.
     */
    @SuppressWarnings("unchecked")
    public void apply (ObserverOp<T> obop)
    {
        int ocount = size();
        if (ocount == 0) {
            return;
        }
        if (_policy == SAFE_IN_ORDER_NOTIFY) {
            // if we have to notify our observers in order, we need to create a snapshot of the
            // observer array at the time we start the notification to ensure that modifications to
            // the array during notification don't hose us
            if (_snap == null || _snap.length < ocount || _snap.length > (ocount << 3)) {
                _snap = (T[])new Object[ocount];
            }
            toArray(_snap);
            for (int ii = 0; ii < ocount; ii++) {
                if (!checkedApply(obop, _snap[ii])) {
                    remove(_snap[ii]);
                }
            }
            // clear out the snapshot so its contents can be gc'd
            Arrays.fill(_snap, null);

        } else if (_policy == FAST_UNSAFE_NOTIFY) {
            for (int ii = ocount-1; ii >= 0; ii--) {
                if (!checkedApply(obop, get(ii))) {
                    remove(ii);
                }
            }
        }
    }

    /**
     * Returns true and issues a warning if this list does not allow duplicates and the supplied
     * observer is already in the list. Returns false if the supplied observer is not a duplicate.
     */
    protected boolean isDuplicate (T obs)
    {
        // make sure we're not violating the list constraints
        if (!_allowDups && contains(obs)) {
            log.warning("Observer attempted to observe list it's already observing!", "obs", obs,
                        new Exception());
            return true;
        }
        return false;
    }

    /**
     * Applies the operation to the observer, catching and logging any exceptions thrown in the
     * process.
     */
    protected static <T> boolean checkedApply (ObserverOp<T> obop, T obs)
    {
        try {
            return obop.apply(obs);
        } catch (Throwable thrown) {
            log.warning("ObserverOp choked during notification", "op", obop, "obs", obs, thrown);
            // if they booched it, definitely don't remove them
            return true;
        }
    }

    /** The notification policy. */
    protected int _policy;

    /** Whether to allow observers to observe more than once simultaneously. */
    protected boolean _allowDups;

    /** Used to avoid creating a new snapshot array every time we notify our observers if the size
     * has not changed. */
    protected T[] _snap;
}
