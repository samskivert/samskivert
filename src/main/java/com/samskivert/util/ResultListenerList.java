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
