//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * A handy base class for issuing server-side service requests and awaiting their responses from
 * within a servlet.
 *
 * <em>Note:</em> You might think that it would be keen to use this anonymously like so:
 *
 * <pre>
 * ServiceWaiter waiter = new ServiceWaiter() {
 *     public void handleSuccess (int invid, ...) {
 *         // handle success
 *     }
 *     // ...
 * };
 * </pre>
 *
 * But that won't work because public methods in anonymous inner classes do not have public
 * visibility and so the invocation manager will not be able to reflect your response methods
 * when the time comes to deliver the response. Unfortunately, there appears to be no manner in
 * which to instruct the Java compiler to give public scope to an anonymous inner class rather
 * than default scope. Sigh.
 */
public class ServiceWaiter<T>
    implements ResultListener<T>
{
    /** Timeout to specify when you don't want a timeout. Use at your own risk. */
    public static final int NO_TIMEOUT = -1;

    public static class TimeoutException extends Exception
    {
        public TimeoutException () {
            super("Timeout! Pow!");
        }
    }

    /**
     * Construct a ServiceWaiter with the default (30 second) timeout.
     */
    public ServiceWaiter ()
    {
        this(DEFAULT_WAITER_TIMEOUT);
    }

    /**
     * Construct a ServiceWaiter with the specified timeout.
     *
     * @param timeout the timeout, in seconds.
     */
    public ServiceWaiter (int timeout)
    {
        setTimeout(timeout);
    }

    /**
     * Change the timeout being used for this ServiceWaiter after it has been constructed.
     */
    public void setTimeout (int timeout)
    {
        _timeout = timeout;
    }

    /**
     * Reset the service waiter so that it can be used again.
     */
    public synchronized void reset ()
    {
        _success = 0;
        _argument = null;
    }

    /**
     * Marks the request as successful and posts the supplied response argument for perusal by the
     * caller.
     */
    public synchronized void postSuccess (T arg)
    {
        _success = 1;
        _argument = arg;
        notify();
    }

    /**
     * Marks the request as failed.
     */
    public synchronized void postFailure (Exception error)
    {
        _success = -1;
        _error = error;
        notify();
    }

    /**
     * Returns the argument posted by the waiter when the response arrived.
     */
    public T getArgument ()
    {
        return _argument;
    }

    /**
     * Returns the exception posted in the event of failure.
     */
    public Exception getError ()
    {
        return _error;
    }

    /**
     * Blocks waiting for the response.
     *
     * @return true if a success response was posted, false if a failure response was posted.
     */
    public boolean waitForResponse ()
        throws TimeoutException
    {
        if (_success == 0) {
            synchronized (this) {
                try {
                    // wait for the response, timing out after a while
                    if (_timeout == NO_TIMEOUT) {
                        wait();

                    } else {
                        wait(1000L * _timeout);
                    }
                    // if we get here without some sort of response, then we've timed out
                    if (_success == 0) {
                        throw new TimeoutException();
                    }

                } catch (InterruptedException ie) {
                    throw (TimeoutException)
                        new TimeoutException().initCause(ie);
                }
            }
        }

        return (_success > 0);
    }

    // documentation inherited from interface ResultListener
    public void requestCompleted (T result)
    {
        postSuccess(result);
    }

    // documentation inherited from interface ResultListener
    public void requestFailed (Exception cause)
    {
        postFailure(cause);
    }

    /** Whether or not the response succeeded; positive for success, negative for failure, zero
     * means we haven't received the response yet. */
    protected int _success = 0;

    /** The argument posted by the waiter upon receipt of the response. */
    protected T _argument;

    /** The exception posted by the waiter upon failure. */
    protected Exception _error;

    /** How many seconds to wait before giving up the ghost. */
    protected int _timeout;

    /** If a response is not received within the specified timeout, an exception is thrown which
     * redirects the user to an internal error page. */
    protected static final int DEFAULT_WAITER_TIMEOUT = 30;
}
