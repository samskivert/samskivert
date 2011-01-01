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

import java.lang.ref.WeakReference;

import java.util.AbstractList;

import com.samskivert.util.ObserverList.ObserverOp;

/**
 * An {@link ObserverList} equivalent that does not prevent added observers from being
 * garbage-collected.
 */
public class WeakObserverList<T> extends AbstractList<T>
{
    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> WeakObserverList<T> newSafeInOrder ()
    {
        return new WeakObserverList<T>(ObserverList.SAFE_IN_ORDER_NOTIFY);
    }

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> WeakObserverList<T> newFastUnsafe ()
    {
        return new WeakObserverList<T>(ObserverList.FAST_UNSAFE_NOTIFY);
    }

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> WeakObserverList<T> newList (int notifyPolicy, boolean allowDups)
    {
        return new WeakObserverList<T>(notifyPolicy, allowDups);
    }

    /**
     * Creates an empty observer list with the supplied notification
     * policy and that only allows observers to observe the list once.
     *
     * @param notifyPolicy Either {@link ObserverList#SAFE_IN_ORDER_NOTIFY} or {@link
     * ObserverList#FAST_UNSAFE_NOTIFY}.
     */
    public WeakObserverList (int notifyPolicy)
    {
        this(notifyPolicy, false);
    }

    /**
     * Creates an empty observer list with the supplied notification and
     * duplicate observer policy.
     *
     * @param notifyPolicy Either {@link ObserverList#SAFE_IN_ORDER_NOTIFY} or {@link
     * ObserverList#FAST_UNSAFE_NOTIFY}.
     * @param allowDups whether to allow observers to be added to the list
     * more than once.
     */
    public WeakObserverList (int notifyPolicy, boolean allowDups)
    {
        _wrappedList = new WrappedList<T>(notifyPolicy, allowDups);
    }

    @Override // documentation inherited
    public int size ()
    {
        return _wrappedList.size();
    }

    @Override // documentation inherited
    public boolean contains (Object element)
    {
        @SuppressWarnings("unchecked") T value = (T)element;
        return _wrappedList.contains(new WeakReference<T>(value));
    }

    @Override // documentation inherited
    public boolean remove (Object element)
    {
        @SuppressWarnings("unchecked") T value = (T)element;
        return _wrappedList.remove(new WeakReference<T>(value));
    }

    @Override // documentation inherited
    public int indexOf (Object element)
    {
        @SuppressWarnings("unchecked") T value = (T)element;
        return _wrappedList.indexOf(new WeakReference<T>(value));
    }

    @Override // documentation inherited
    public int lastIndexOf (Object element)
    {
        @SuppressWarnings("unchecked") T value = (T)element;
        return _wrappedList.lastIndexOf(new WeakReference<T>(value));
    }

    @Override // documentation inherited
    public T get (int index)
    {
        return _wrappedList.get(index).get();
    }

    @Override // documentation inherited
    public T set (int index, T element)
    {
        return _wrappedList.set(index, new WeakReference<T>(element)).get();
    }

    @Override // documentation inherited
    public void add (int index, T element)
    {
        _wrappedList.add(index, new WeakReference<T>(element));
    }

    @Override // documentation inherited
    public T remove (int index)
    {
        return _wrappedList.remove(index).get();
    }

    /**
     * Applies the supplied observer operation to all observers in the
     * list in a manner conforming to the notification ordering policy
     * specified at construct time.
     */
    public void apply (ObserverOp<T> obop)
    {
        _derefOp.init(obop);
        _wrappedList.apply(_derefOp);
    }

    /**
     * Removes all garbage-collected observers from the list.
     */
    public void prune ()
    {
        for (int ii = _wrappedList.size() - 1; ii >= 0; ii--) {
            if (_wrappedList.get(ii).get() == null) {
                _wrappedList.remove(ii);
            }
        }
    }

    /**
     * An operation that resolves a reference and applies a wrapped op.
     */
    protected static class DerefOp<T>
        implements ObserverOp<WeakReference<T>>
    {
        /**
         * (Re)initializes this op with a reference to the wrapped op.
         */
        public void init (ObserverOp<T> op)
        {
            _op = op;
        }

        // documentation inherited from interface ObserverOp
        public boolean apply (WeakReference<T> ref)
        {
            T observer = ref.get();
            return observer != null && _op.apply(observer);
        }

        /** The wrapped op. */
        protected ObserverOp<T> _op;
    }

    /**
     * ObserverList extension that dereferences elements when searching for a value.
     */
    protected static class WrappedList<T> extends ObserverList<WeakReference<T>>
    {
        public WrappedList (int notifyPolicy, boolean allowDups) {
            super(notifyPolicy, allowDups);
        }

        @Override public int indexOf (Object element) {
            @SuppressWarnings("unchecked") WeakReference<T> ref = (WeakReference<T>)element;
            T value = ref.get();
            for (int ii = 0, nn = size(); ii < nn; ii++) {
                if (value == get(ii).get()) {
                    return ii;
                }
            }
            return -1;
        }

        @Override public int lastIndexOf (Object element) {
            @SuppressWarnings("unchecked") WeakReference<T> ref = (WeakReference<T>)element;
            T value = ref.get();
            for (int ii = size() - 1; ii >= 0; ii--) {
                if (value == get(ii).get()) {
                    return ii;
                }
            }
            return -1;
        }
    }

    /** The wrapped list. */
    protected WrappedList<T> _wrappedList;

    /** The wrapper op. */
    protected DerefOp<T> _derefOp = new DerefOp<T>();
}
