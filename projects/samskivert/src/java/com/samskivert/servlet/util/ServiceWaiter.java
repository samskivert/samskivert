//
// $Id: ServiceWaiter.java,v 1.4 2003/08/15 03:07:52 ray Exp $

package com.samskivert.servlet.util;

/**
 * Extends the basic ServiceWaiter to be useful for servlets.
 */
public class ServiceWaiter extends com.samskivert.util.ServiceWaiter
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
