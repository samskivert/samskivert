//
// $Id: NestableIOException.java,v 1.3 2003/03/17 23:01:37 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * A convenience class for creating IO exceptions that are caused by other
 * exceptions.
 */
public class NestableIOException extends IOException
{
    /**
     * Constructs a new <code>NestableIOException</code> with specified
     * nested <code>Throwable</code>.
     *
     * @param cause the exception or error that caused this exception to
     * be thrown.
     */
    public NestableIOException (Throwable cause)
    {
        initCause(cause);
    }

    /**
     * Constructs a new <code>NestableIOException</code> with specified
     * detail message and nested <code>Throwable</code>.
     *
     * @param msg the error message.
     * @param cause the exception or error that caused this exception to
     * be thrown.
     */
    public NestableIOException (String msg, Throwable cause)
    {
        super(msg);
        initCause(cause);
    }
}
