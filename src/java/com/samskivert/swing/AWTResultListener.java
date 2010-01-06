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
