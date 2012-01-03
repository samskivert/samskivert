//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * Multiplexes ResultListener responses to multiple ResultListeners.
 */
public class ResultListenerList<T> extends ObserverList.Impl<ResultListener<T>>
    implements ResultListener<T>
{
    /**
     * Create a ResultListenerList with the FAST_UNSAFE notification policy.
     */
    public ResultListenerList ()
    {
        super(Policy.FAST_UNSAFE);
    }

    /**
     * Create a ResultListenerList with your own notifyPolicy.
     */
    public ResultListenerList (Policy notifyPolicy)
    {
        super(notifyPolicy);
    }

    /**
     * Multiplex a requestCompleted response to all the ResultListeners in
     * this list.
     */
    public void requestCompleted (final T result)
    {
        apply(new ObserverOp<ResultListener<T>>() {
            public boolean apply (ResultListener<T> observer) {
                observer.requestCompleted(result);
                return true;
            }
        });
    }

    /**
     * Multiplex a requestFailed response to all the ResultListeners in
     * this list.
     */
    public void requestFailed (final Exception cause)
    {
        apply(new ObserverOp<ResultListener<T>>() {
            public boolean apply (ResultListener<T> observer) {
                observer.requestFailed(cause);
                return true;
            }
        });
    }
}
