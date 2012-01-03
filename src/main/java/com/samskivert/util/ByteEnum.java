//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * An interface implemented by enums that can map themselves to a byte. See {@link ByteEnumUtil}
 * for mapping back from a byte to an enum.
 */
public interface ByteEnum
{
    /**
     * Returns the byte value to which to map this enum value.
     */
    public byte toByte ();
}
