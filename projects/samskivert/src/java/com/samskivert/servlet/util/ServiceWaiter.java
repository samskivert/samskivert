//
// $Id: ServiceWaiter.java,v 1.3 2003/08/15 02:23:47 ray Exp $

package com.samskivert.servlet.util;

import com.samskivert.servlet.RedirectException;

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
    public boolean awaitResponse ()
        throws RedirectException
    {
        try {
            return waitForResponse();

        } catch (TimeoutException te) {
            throw new RedirectException(getTimeoutRedirectURL());
        }
    }

    /**
     * Get the page to which we redirect on timeout.
     */
    protected String getTimeoutRedirectURL ()
    {
        // TODO
        return "/";
    }
}
