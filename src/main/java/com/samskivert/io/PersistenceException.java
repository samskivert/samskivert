//
// $Id$
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

package com.samskivert.io;

/**
 * A persistence exception can be thrown when an error occurs in
 * underlying persistence code. By encapsulating errors, one retains the
 * ability to make changes to the implementation structure without
 * affecting the interface to persistence services presented to the
 * application.
 */
public class PersistenceException extends Exception
{
    /**
     * Constructs a persistence exception with the specified error
     * message.
     */
    public PersistenceException (String message)
    {
        super(message);
    }

    /**
     * Constructs a persistence exception with the specified error message
     * and the chained causing event.
     */
    public PersistenceException (String message, Exception cause)
    {
        super(message);
        initCause(cause);
    }

    /**
     * Constructs a persistence exception with the specified chained
     * causing event.
     */
    public PersistenceException (Exception cause)
    {
        initCause(cause);
    }
}
