//
// $Id$

package com.samskivert.swing;

import java.awt.EventQueue;

import com.samskivert.util.ResultListener;

/**
 * Dispatches a {@link ResultListener}'s callbacks on the AWT thread
 * regardless of what thread on which they were originally dispatched.
 */
public class AWTResultListener implements ResultListener
{
    /**
     * Creates an AWT result listener that will dispatch results to the
     * supplied target.
     */
    public AWTResultListener (ResultListener target)
    {
        _target = target;
    }

    // documentation inherited from interface
    public void requestCompleted (final Object result)
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
    protected ResultListener _target;
}
