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
public class WeakObserverList<T> extends ObserverList<T>
{
    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> WeakObserverList<T> newSafeInOrder ()
    {
        return newList(Policy.SAFE_IN_ORDER, false);
    }

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> WeakObserverList<T> newFastUnsafe ()
    {
        return newList(Policy.FAST_UNSAFE, false);
    }

    /**
     * A convenience method for creating an observer list that avoids duplicating the type
     * parameter on the right hand side.
     */
    public static <T> WeakObserverList<T> newList (Policy notifyPolicy, boolean allowDups)
    {
        return new WeakObserverList<T>(notifyPolicy, allowDups);
    }

    @Override public boolean add (int index, T element)
    {
        return _delegate.add(index, new WeakReference<T>(element));
    }

    @Override public boolean add (T element)
    {
        return _delegate.add(new WeakReference<T>(element));
    }

    @Override public boolean remove (T element)
    {
        return _delegate.remove(new WeakReference<T>(element));
    }

    @Override public void apply (ObserverOp<T> obop)
    {
        _derefOp.init(obop);
        _delegate.apply(_derefOp);
    }

    @Override public int size ()
    {
        return _delegate.size();
    }

    @Override public boolean isEmpty ()
    {
        return _delegate.isEmpty();
    }

    @Override public void clear ()
    {
        _delegate.clear();
    }

    /**
     * Removes all garbage-collected observers from the list.
     */
    public void prune ()
    {
        // applying an op prunes collected observers, so just apply a NOOP op
        apply(new ObserverOp<T>() {
            public boolean apply (T obs) {
                return true;
            }
        });
    }

    protected WeakObserverList (Policy notifyPolicy, boolean allowDups)
    {
        _delegate = new WrappedList<T>(notifyPolicy, allowDups);
    }

    /**
     * An operation that resolves a reference and applies a wrapped op.
     */
    protected static class DerefOp<T> implements ObserverOp<WeakReference<T>>
    {
        /** (Re)initializes this op with a reference to the wrapped op. */
        public void init (ObserverOp<T> op) {
            _op = op;
        }

        // documentation inherited from interface ObserverOp
        public boolean apply (WeakReference<T> ref) {
            T observer = ref.get();
            return observer != null && _op.apply(observer);
        }

        /** The wrapped op. */
        protected ObserverOp<T> _op;
    }

    /**
     * ObserverList extension that dereferences elements when searching for a value.
     */
    protected static class WrappedList<T> extends ObserverList.Impl<WeakReference<T>>
    {
        public WrappedList (Policy notifyPolicy, boolean allowDups) {
            super(notifyPolicy, allowDups);
        }

        @Override protected int indexOf (WeakReference<T> ref) {
            T value = ref.get();
            for (int ii = 0, ll = _list.size(); ii < ll; ii++) {
                if (_list.get(ii).get() == value) return ii;
            }
            return -1;
        }
    }

    /** A delegate list that contains weak reference wrapped elements. */
    protected WrappedList<T> _delegate;

    /** The wrapper op. */
    protected DerefOp<T> _derefOp = new DerefOp<T>();
}
