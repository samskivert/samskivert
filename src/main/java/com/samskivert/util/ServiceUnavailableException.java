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
 * The service unavailable exception can be thrown by any service that
 * relies on some other services which is currently not available. If a
 * service, for example, required information from a database and the
 * underlying database service failed, it might wish to throw a service
 * unavailable exception rather than provide invalid data to the caller.
 *
 * <p> Because this is a runtime exception, it is expected that it would
 * only be thrown in unsalvagable situations and with the knowledge that
 * some entity in the calling application would eventually catch and
 * report the exception in as sensible way as possible (it is in the same
 * class of error recovery as if our service threw a
 * <code>NullPointerException</code>).
 */
public class ServiceUnavailableException extends RuntimeException
{
    /**
     * Constructs a service unavailable exception with the specified error
     * message.
     */
    public ServiceUnavailableException (String message)
    {
        super(message);
    }

    /**
     * Constructs a service unavailable exception with the specified error
     * message and the chained causing event.
     */
    public ServiceUnavailableException (String message, Exception cause)
    {
        super(message);
        initCause(cause);
    }

    /**
     * Constructs a service unavailable exception with the specified
     * chained causing event.
     */
    public ServiceUnavailableException (Exception cause)
    {
        initCause(cause);
    }
}
