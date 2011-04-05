//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
public abstract class ObserverList<T>
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

    /** Notification policies. */
    public enum Policy {
        /** A notification ordering policy indicating that the observers should be notified in the
         * order they were added and that the notification should be done on a snapshot of the
         * array. */
        SAFE_IN_ORDER,

        /** A notification ordering policy wherein the observers are notified last to first so that
         * they can be removed during the notification process and new observers added will not
         * inadvertently be notified as well, but no copy of the observer list need be made. This
         * will not work if observers are added or removed from arbitrary positions in the list
         * during a notification call. */
        FAST_UNSAFE;
    };

    /** @deprecated Use {@link Policy.SAFE_IN_ORDER}. */
    @Deprecated public static final int SAFE_IN_ORDER_NOTIFY = 1;

    /** @deprecated Use {@link Policy.FAST_UNSAFE}. */
    @Deprecated public static final int FAST_UNSAFE_NOTIFY = 2;

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> ObserverList<T> newSafeInOrder ()
    {
        return newList(Policy.SAFE_IN_ORDER, false);
    }

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> ObserverList<T> newFastUnsafe ()
    {
        return newList(Policy.FAST_UNSAFE, false);
    }

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> ObserverList<T> newList (Policy notifyPolicy, boolean allowDups)
    {
        return new Impl<T>(notifyPolicy, allowDups);
    }

    /** @deprecated Switch to {@link Policy} constants. */
    @Deprecated public static <T> ObserverList<T> newList (int notifyPolicy, boolean allowDups)
    {
        switch (notifyPolicy) {
        case SAFE_IN_ORDER_NOTIFY: return newList(Policy.SAFE_IN_ORDER, allowDups);
        case FAST_UNSAFE_NOTIFY: return newList(Policy.FAST_UNSAFE, allowDups);
        default: throw new IllegalArgumentException("Unknown policy " + notifyPolicy);
        }
    }

    /**
     * Adds an observer at the specified index.
     * @return true if the observer was added, false if it was already in the list.
     */
    public abstract boolean add (int index, T element);

    /**
     * Adds an observer to the end of the list.
     * @return true if the observer was added, false if it was already in the list.
     */
    public abstract boolean add (T element);

    /**
     * Removes the specified observer from the list.
     * @return true if the observer was located and removed, false if it does not exist.
     */
    public abstract boolean remove (T element);

    /**
     * Applies the supplied observer operation to all observers in the list in a manner conforming
     * to the notification ordering policy specified at construct time.
     */
    public abstract void apply (ObserverOp<T> obop);

    /**
     * Returns the number of observers in this list.
     */
    public abstract int size ();

    /**
     * Clears all observers from the list.
     */
    public abstract void clear ();

    /**
     * Returns true if this list has no observers, false otherwise.
     */
    public boolean isEmpty ()
    {
        return size() == 0;
    }

    protected static class Impl<T> extends ObserverList<T> {
        protected Impl (Policy notifyPolicy, boolean allowDups) {
            _policy = notifyPolicy;
            _allowDups = allowDups;
            _list = (_policy == Policy.SAFE_IN_ORDER) ?
                new CopyOnWriteArrayList<T>() : new ArrayList<T>();
        }

        @Override public boolean add (int index, T element) {
            if (element == null) throw new NullPointerException("Null observers not allowed.");
            if (isDuplicate(element)) return false;
            _list.add(index, element);
            return true;
        }

        @Override public boolean add (T element) {
            if (element == null) throw new NullPointerException("Null observers not allowed.");
            if (isDuplicate(element)) return false;
            _list.add(element);
            return true;
        }

        @Override public boolean remove (T element) {
            int idx = indexOf(element);
            if (idx < 0) return false;
            _list.remove(idx);
            return true;
        }

        @Override public void apply (ObserverOp<T> obop) {
            switch (_policy) {
            case SAFE_IN_ORDER:
                // our copy on write array list will prevent us from getting hosed if modifications
                // take place during iteration
                Iterator<T> iter = _list.iterator();
                for (int ii = 0; iter.hasNext(); ii++) {
                    T elem = iter.next();
                    if (!checkedApply(obop, elem)) {
                        // can't remove via COWArrayList iterator, and to be totally safe (because
                        // we don't know if any additions or removals have taken place while we
                        // were iterating), we have to scan the whole list to locate this element
                        // so that we can remove it
                        remove(elem);
                    }
                }
                break;

            case FAST_UNSAFE:
                for (int ii = _list.size()-1; ii >= 0; ii--) {
                    if (!checkedApply(obop, _list.get(ii))) {
                        _list.remove(ii);
                    }
                }
                break;
            }
        }

        @Override public int size () {
            return _list.size();
        }

        @Override public void clear () {
            _list.clear();
        }

        /** Used to determine whether an element is in the list. */
        protected int indexOf (T element) {
            for (int ii = 0, ll = _list.size(); ii < ll; ii++) {
                if (_list.get(ii) == element) return ii;
            }
            return -1;
        }

        /** Returns true and issues a warning if this list does not allow duplicates and the
         * supplied observer is already in the list. Returns false if the supplied observer is not
         * a duplicate. */
        protected boolean isDuplicate (T obs) {
            // make sure we're not violating the list constraints
            if (!_allowDups && (indexOf(obs) >= 0)) {
                log.warning("Observer attempted to observe list it's already observing!", "obs", obs,
                            new Exception());
                return true;
            }
            return false;
        }

        /** The notification policy. */
        protected Policy _policy;

        /** Whether to allow observers to observe more than once simultaneously. */
        protected boolean _allowDups;

        /** Our list of observers. */
        protected List<T> _list;
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
}
