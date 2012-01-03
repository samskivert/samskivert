//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * A result listener that contains another result listener to which failure is
 * passed directly, but allows for success to be handled in whatever way is
 * desired by the chaining result listener.
 */
public abstract class ChainedResultListener<T,TT>
    implements ResultListener<T>
{
    /**
     * Creates a chained result listener that will pass failure through to the
     * specified target.
     */
    public ChainedResultListener (ResultListener<TT> target)
    {
        _target = target;
    }

    // documentation inherited from interface ResultListener
    public void requestFailed (Exception cause)
    {
        _target.requestFailed(cause);
    }

    protected ResultListener<TT> _target;
}
