//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.EnumSet;
import java.util.Set;

/**
 * {@link ByteEnum} utility methods.
 */
public class ByteEnumUtil
{
    /**
     * Returns the enum value with the specified code in the supplied enum class.
     *
     * @exception IllegalArgumentException thrown if the enum lacks a value that maps to the
     * supplied code.
     */
    public static <E extends Enum<E> & ByteEnum> E fromByte (Class<E> eclass, byte code)
    {
        for (E value : eclass.getEnumConstants()) {
            if (value.toByte() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException(eclass + " has no value with code " + code);
    }

    /**
     * Convert a Set of ByteEnums into an integer compactly representing the elements that are
     * included.
     */
    public static <E extends Enum<E> & ByteEnum> int setToInt (Set<E> set)
    {
        int flags = 0;
        for (E value : set) {
            flags |= toIntFlag(value);
        }
        return flags;
    }

    /**
     * Convert an int representation of ByteEnum flags into an EnumSet.
     */
    public static <E extends Enum<E> & ByteEnum> EnumSet<E> intToSet (Class<E> eclass, int flags)
    {
        EnumSet<E> set = EnumSet.noneOf(eclass);
        for (E value : eclass.getEnumConstants()) {
            if ((flags & toIntFlag(value)) != 0) {
                set.add(value);
            }
        }
        return set;
    }

    /**
     * A helper function for setToInt() and intToSet() that validates that the specified
     * ByteEnum value is not null and has a code between 0 and 31, inclusive.
     */
    protected static <E extends Enum<E> & ByteEnum> int toIntFlag (E value)
    {
        byte code = value.toByte(); // allow this to throw NPE
        if (code < 0 || code > 31) {
            throw new IllegalArgumentException(
                "ByteEnum code is outside the range that can be turned into an int " +
                "[value=" + value + ", code=" + code + "]");
        }
        return (1 << code);
    }

    // TODO: setToByteArray() and byteArrayToSet(), for larger ByteEnums?
}
