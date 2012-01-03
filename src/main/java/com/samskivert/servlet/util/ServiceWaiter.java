//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.util;

/**
 * Extends the basic ServiceWaiter to be useful for servlets.
 */
public class ServiceWaiter<T> extends com.samskivert.util.ServiceWaiter<T>
{
    /** Timeout to specify when you don't want a timeout. Use at your own
     * risk. */
    public static final int NO_TIMEOUT = -1;

    /**
     * Construct a ServiceWaiter with the default (30 second) timeout.
     */
    public ServiceWaiter ()
    {
        super();
    }

    /**
     * Construct a ServiceWaiter with the specified timeout.
     *
     * @param timeout the timeout, in seconds.
     */
    public ServiceWaiter (int timeout)
    {
        super(timeout);
    }

    /**
     * Blocks waiting for the response.
     *
     * @return true if a success response was posted, false if a failure
     * repsonse was posted.
     */
    public boolean awaitFriendlyResponse (String friendlyText)
        throws FriendlyException
    {
        try {
            return waitForResponse();
        } catch (TimeoutException te) {
            throw new FriendlyException(friendlyText);
        }
    }
}
