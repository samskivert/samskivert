//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2008 Michael Bayne
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

package com.samskivert.jdbc.depot;

/**
 * Represents a failure reported by the underlying database.
 */
public class DatabaseException extends RuntimeException
{
    /**
     * Constructs a database exception with the specified error message.
     */
    public DatabaseException (String message)
    {
        super(message);
    }

    /**
     * Constructs a database exception with the specified error message and the chained causing
     * event.
     */
    public DatabaseException (String message, Throwable cause)
    {
        super(message);
        initCause(cause);
    }

    /**
     * Constructs a database exception with the specified chained causing event.
     */
    public DatabaseException (Throwable cause)
    {
        initCause(cause);
    }
}
