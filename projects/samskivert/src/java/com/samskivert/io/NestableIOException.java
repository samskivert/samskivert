//
// $Id: NestableIOException.java,v 1.1 2001/11/08 01:17:41 mdb Exp $
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

import org.apache.commons.util.exception.Nestable;
import org.apache.commons.util.exception.NestableDelegate;

/**
 * A base class for IO exceptions that can contain other exceptions.
 *
 * @see org.apache.commons.util.exception.NestableException
 */
public class NestableIOException
    extends IOException
    implements Nestable
{
    /**
     * Constructs a new <code>NestableIOException</code> without specified
     * detail message.
     */
    public NestableIOException ()
    {
    }

    /**
     * Constructs a new <code>NestableIOException</code> with specified
     * detail message.
     *
     * @param msg the error message.
     */
    public NestableIOException (String msg)
    {
        super(msg);
    }

    /**
     * Constructs a new <code>NestableIOException</code> with specified
     * nested <code>Throwable</code>.
     *
     * @param cause the exception or error that caused this exception to
     * be thrown.
     */
    public NestableIOException (Throwable cause)
    {
        super();
        _cause = cause;
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
        _cause = cause;
    }

    /**
     * Returns the nested exception.
     *
     * @see Nestable#getCause
     */
    public Throwable getCause ()
    {
        return _cause;
    }

    /**
     * Concatenates this exception's message with that of the nested
     * exception and returns it.
     *
     * @see Nestable#getMessage
     */
    public String getMessage ()
    {
        StringBuffer msg = new StringBuffer();
        String ourMsg = super.getMessage();

        // add our message (if there is one)
        if (ourMsg != null) {
            msg.append(ourMsg);
        }

        // add our cause's message (if there is one)
        if (_cause != null) {
            String causeMsg = _cause.getMessage();
            if (causeMsg != null) {
                if (ourMsg != null) {
                    msg.append(": ");
                }
                msg.append(causeMsg);
            }
        }

        return (msg.length() > 0 ? msg.toString() : null);
    }

    /**
     * Prints the stack trace of this exception (and the nested exception)
     * the the standard error stream.
     *
     * @see Nestable#printStackTrace()
     */
    public void printStackTrace ()
    {
        _delegate.printStackTrace();
    }

    /**
     * Prints the stack trace of this exception (and the nested exception)
     * to the specified print stream.
     *
     * @param out <code>PrintStream</code> to use for output.
     *
     * @see Nestable#printStackTrace(PrintStream)
     */
    public void printStackTrace (PrintStream out)
    {
        _delegate.printStackTrace(out);
    }

    /**
     * Prints the stack trace of this exception (and the nested exception)
     * to the specified print writer.
     *
     * @param out <code>PrintWriter</code> to use for output.
     *
     * @see Nestable#printStackTrace(PrintWriter)
     */
    public void printStackTrace (PrintWriter out)
    {
        _delegate.printStackTrace(out);
    }

    /**
     * Prints the stack trace only of the enclosing exception.
     *
     * @see Nestable#printPartialStackTrace(PrintWriter)
     */
    public final void printPartialStackTrace (PrintWriter out)
    {
        super.printStackTrace(out);
    }

    /** The helper instance which contains much of the code to which we
     * delegate. */
    protected NestableDelegate _delegate = new NestableDelegate(this);

    /** Holds the reference to the exception or error that caused this
     * exception to be thrown. */
    protected Throwable _cause = null;
}
