//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.EventQueue;

import com.samskivert.util.ResultListener;

/**
 * Dispatches a {@link ResultListener}'s callbacks on the AWT thread
 * regardless of what thread on which they were originally dispatched.
 */
public class AWTResultListener<T> implements ResultListener<T>
{
    /**
     * Creates an AWT result listener that will dispatch results to the
     * supplied target.
     */
    public AWTResultListener (ResultListener<T> target)
    {
        _target = target;
    }

    // documentation inherited from interface
    public void requestCompleted (final T result)
    {
        EventQueue.invokeLater(new Runnable() {
            public void run () {
                _target.requestCompleted(result);
            }
        });
    }

    // documentation inherited from interface
    public void requestFailed (final Exception cause)
    {
        EventQueue.invokeLater(new Runnable() {
            public void run () {
                _target.requestFailed(cause);
            }
        });
    }

    /** The result listener for which we are proxying. */
    protected ResultListener<T> _target;
}
