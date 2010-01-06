//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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
import java.io.UnsupportedEncodingException;

/**
 * Wraps a supplied {@link PrintStream} to allow capturing all data
 * written to the stream via the various <code>print()</code> and
 * <code>println()</code> variants.  Derived classes must implement the
 * {@link #handlePrinted} and {@link #handleNewLine} methods.
 */
public abstract class ExtensiblePrintStream extends PrintStream
{
    /**
     * Constructs an extensible print stream.
     */
    public ExtensiblePrintStream (PrintStream out)
    {
        super(out);
    }

    /**
     * Constructs an extensible print stream.
     */
    public ExtensiblePrintStream (PrintStream out, boolean autoFlush)
    {
        super(out, autoFlush);
    }

    /**
     * Constructs an extensible print stream.
     */
    public ExtensiblePrintStream (
        PrintStream out, boolean autoFlush, String encoding)
        throws UnsupportedEncodingException
    {
        super(out, autoFlush, encoding);
    }

    /**
     * Called with any text printed to the stream, excepting newlines
     * resulting from calls to the various <code>println()</code> methods,
     * which are reported via {@link #handleNewLine}.
     */
    public abstract void handlePrinted (String s);

    /**
     * Called whenever a newline is printed to the stream by one of the
     * various <code>println()</code> methods.
     */
    public abstract void handleNewLine ();

    @Override
    public void print (boolean b)
    {
        super.print(b);
        handlePrinted(b ? "true" : "false");
    }

    @Override
    public void print (char c)
    {
        super.print(c);
	handlePrinted(String.valueOf(c));
    }

    @Override
    public void print (int i)
    {
        super.print(i);
	handlePrinted(String.valueOf(i));
    }

    @Override
    public void print (long l)
    {
        super.print(l);
	handlePrinted(String.valueOf(l));
    }

    @Override
    public void print (float f)
    {
        super.print(f);
	handlePrinted(String.valueOf(f));
    }

    @Override
    public void print (double d)
    {
        super.print(d);
	handlePrinted(String.valueOf(d));
    }

    @Override
    public void print (char[] s)
    {
        super.print(s);
	handlePrinted(String.valueOf(s));
    }

    @Override
    public void print (String s)
    {
        super.print(s);
	handlePrinted((s == null) ? "null" : s);
    }

    @Override
    public void print (Object obj)
    {
        super.print(obj);
	handlePrinted(String.valueOf(obj));
    }

    @Override
    public void println ()
    {
        super.println();
        handleNewLine();
    }

    @Override
    public void println (boolean x)
    {
        super.println(x);
        handleNewLine();
    }

    @Override
    public void println (char x)
    {
        super.println(x);
        handleNewLine();
    }

    @Override
    public void println (int x)
    {
        super.println(x);
        handleNewLine();
    }

    @Override
    public void println (long x)
    {
        super.println(x);
        handleNewLine();
    }

    @Override
    public void println (float x)
    {
        super.println(x);
        handleNewLine();
    }

    @Override
    public void println (double x)
    {
        super.println(x);
        handleNewLine();
    }

    @Override
    public void println (char[] x)
    {
        super.println(x);
        handleNewLine();
    }

    @Override
    public void println (String x)
    {
        super.println(x);
        handleNewLine();
    }

    @Override
    public void println (Object x)
    {
        super.println(x);
        handleNewLine();
    }
}
