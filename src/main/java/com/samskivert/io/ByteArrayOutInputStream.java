//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * The byte array out/input stream is used for writing data to a byte
 * array output stream and then obtaining an input stream that can read
 * back the data written to the output stream without first having to copy
 * it to a separate buffer.
 */
public class ByteArrayOutInputStream extends ByteArrayOutputStream
{
    /**
     * Creates a new byte array out/input stream. The buffer capacity is
     * initially 32 bytes, though its size increases if necessary.
     */
    public ByteArrayOutInputStream ()
    {
        this(32);
    }

    /**
     * Creates a new byte array out/input stream, with a buffer capacity
     * of the specified size, in bytes.
     *
     * @param size the initial size.
     *
     * @exception IllegalArgumentException if size is negative.
     */
    public ByteArrayOutInputStream (int size)
    {
        super(size);
    }

    /**
     * Returns an input stream configured to read only the bytes that have
     * been written this far to this byte array out/input stream.
     */
    public InputStream getInputStream ()
    {
        return new ByteArrayInputStream(buf, 0, count);
    }
}
