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

package com.samskivert.util;

/**
 * Acts as a {@link ResultListenerList} until the result (or cause of failure) is received,
 * after which {@link #getResult} provides the result immediately.  This is most useful for
 * caches in which multiple listeners may request the result while it is resolving.
 */
public class ResultHandler<T>
    implements ResultListener<T>
{
    /**
     * Retrieves the result for the specified listener.  If the result is already available (or the
     * request failed), the listener will receive an immediate response.  Otherwise, the listener
     * will be added to the list and notified when the result is available.
     */
    public void getResult (ResultListener<T> rl)
    {
        if (_error != null) {
            rl.requestFailed(_error);
        } else if (_list == null) { // _list == null when we have a result
            rl.requestCompleted(_result);
        } else {
            _list.add(rl);
        }
    }

    /**
     * Checks whether the result (or error, if the request failed) is already available.
     */
    public boolean hasResult ()
    {
        return (_list == null);
    }

    /**
     * Peeks at the result, which will be returned if already available.  If the result is pending
     * or there was an error, this method returns <code>null</code> (which, however, will also be
     * returned if the actual result was <code>null</code>).
     */
    public T peekResult ()
    {
        return _result;
    }

    // documentation inherited from interface ResultListener
    public void requestCompleted (T result)
    {
        _result = result;
        ResultListener<T> list = _list;
        _list = null;

        list.requestCompleted(result);
    }

    // documentation inherited from interface ResultListener
    public void requestFailed (Exception cause)
    {
        _error = cause;
        ResultListener<T> list = _list;
        _list = null;

        list.requestFailed(cause);
    }

    /** If non-null, indicates that we're waiting for the result and holds the list of registered
     * listeners. */
    protected ResultListenerList<T> _list = new ResultListenerList<T>();

    /** If non-null, the cause of the failure to obtain a result. */
    protected Exception _error;

    /** The result. */
    protected T _result;
}
