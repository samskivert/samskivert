//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.lang.ref.WeakReference;

/**
 * An {@link ObserverList} equivalent that does not prevent added observers from being
 * garbage-collected.
 */
public class WeakObserverList<T> extends ObserverList<T>
{
    /**
     * Creates a list with {@link ObserverList.Policy#SAFE_IN_ORDER} notification policy.
     */
    public static <T> WeakObserverList<T> newSafeInOrder ()
    {
        return newList(Policy.SAFE_IN_ORDER);
    }

    /**
     * Creates a list with {@link ObserverList.Policy#FAST_UNSAFE} notification policy.
     */
    public static <T> WeakObserverList<T> newFastUnsafe ()
    {
        return newList(Policy.FAST_UNSAFE);
    }

    /**
     * Creates a weak observer list with the specified notification policy.
     */
    public static <T> WeakObserverList<T> newList (Policy notifyPolicy)
    {
        return new WeakObserverList<T>(notifyPolicy);
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

    @Override public void clear ()
    {
        _delegate.clear();
    }

    @Override public WeakObserverList<T> setCheckDuplicates (boolean checkDuplicates)
    {
        _delegate.setCheckDuplicates(checkDuplicates);
        return this;
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

    protected WeakObserverList (Policy notifyPolicy)
    {
        _delegate = new WrappedList<T>(notifyPolicy);
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
        public WrappedList (Policy notifyPolicy) {
            super(notifyPolicy);
        }

        @Override protected int indexOf (WeakReference<T> ref) {
            T value = ref.get();
            for (int ii = 0, ll = _list.size(); ii < ll; ii++) {
                if (_list.get(ii).get() == value) { return ii; }
            }
            return -1;
        }
    }

    /** A delegate list that contains weak reference wrapped elements. */
    protected WrappedList<T> _delegate;

    /** The wrapper op. */
    protected DerefOp<T> _derefOp = new DerefOp<T>();
}
