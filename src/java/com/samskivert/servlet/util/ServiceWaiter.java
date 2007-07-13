//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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
