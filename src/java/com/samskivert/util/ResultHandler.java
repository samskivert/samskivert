//
// $Id: ResultListener.java,v 1.1 2002/03/20 07:07:48 mdb Exp $

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
        if (_result != null) {
            rl.requestCompleted(_result);
        } else if (_error != null) {
            rl.requestFailed(_error);
        } else { // _list != null
            _list.add(rl);
        }
    }

    // documentation inherited from interface ResultListener
    public void requestCompleted (T result)
    {
        _list.requestCompleted(_result = result);
        _list = null;
    }

    // documentation inherited from interface ResultListener
    public void requestFailed (Exception cause)
    {
        _list.requestFailed(_error = cause);
        _list = null;
    }

    /** When waiting for the result, the list of registered listeners. */
    protected ResultListenerList<T> _list = new ResultListenerList<T>();

    /** The result, if it was obtained successfully. */
    protected T _result;

    /** The cause of the failure, if the result could not be obtained. */
    protected Exception _error;
}
