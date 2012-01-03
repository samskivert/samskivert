//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Comparator;

import com.samskivert.annotation.ReplacedBy;

/**
 * A repository for standard comparators.
 */
public class Comparators
{
    /**
     * A comparator that compares the toString() value of all objects case insensitively.
     */
    @ReplacedBy("com.google.common.collect.Ordering.from(String.CASE_INSENSITIVE_ORDER).onResultOf(Functions.toStringFunction()).nullsLast()")
    public static final Comparator<Object> LEXICAL_CASE_INSENSITIVE = new Comparator<Object>() {
        public int compare (Object o1, Object o2)
        {
            if (o1 == o2) { // catches null == null
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }
            // now that we've filtered all nulls, compare the toString()s
            return String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString());
        }
    };

    /**
     * A comparator that compares {@link Comparable} instances.
     */
    @ReplacedBy("com.google.common.collect.Ordering.natural().nullsLast()")
    public static final Comparator<Comparable<Object>> COMPARABLE =
        new Comparator<Comparable<Object>>() {
        public int compare (Comparable<Object> o1, Comparable<Object> o2)
        {
            if (o1 == o2) { // catches null == null
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }
            return o1.compareTo(o2); // null-free
        }
    };

    /**
     * Returns the Comparator for Comparables, properly cast.
     *
     * <p>This example illustrates the type-safe way to obtain a natural-ordering Comparator:
     * <pre>
     *    Comparator&lt;Integer&gt; = Comparators.comparable();
     * </pre>
     */
    // we can't do the "more correct" <T extends Comparable<? super T>> here as that causes other
    // code to freak out; I don't entirely understand why
    @ReplacedBy("com.google.common.collect.Ordering.natural().nullsLast()")
    public static final <T extends Comparable<?>> Comparator<T> comparable ()
    {
        @SuppressWarnings("unchecked") Comparator<T> comp = (Comparator<T>)COMPARABLE;
        return comp;
    }

    /**
     * Compares two bytes, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Byte.
     */
    @ReplacedBy("com.google.common.primitives.SignedBytes.compare()")
    public static int compare (byte value1, byte value2)
    {
        return (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));
    }

    /**
     * Compares two chars, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Character.
     */
    @ReplacedBy("com.google.common.primitives.Chars.compare()")
    public static int compare (char value1, char value2)
    {
        return (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));
    }

    /**
     * Compares two shorts, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Character.
     */
    @ReplacedBy("com.google.common.primitives.Shorts.compare()")
    public static int compare (short value1, short value2)
    {
        return (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));
    }

    /**
     * Compares two integers in an overflow safe manner, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Integer.
     */
    @ReplacedBy("com.google.common.primitives.Ints.compare()")
    public static int compare (int value1, int value2)
    {
        return (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));
    }

    /**
     * Compares two longs in an overflow safe manner, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Long.
     */
    @ReplacedBy("com.google.common.primitives.Longs.compare()")
    public static int compare (long value1, long value2)
    {
        return (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));
    }

    /**
     * Returns the first non-zero value in the supplied list. This is useful for combining
     * comparators:
     * <pre>
     * return Comparators.compare(name.compareTo(oname), Comparators.compare(price, oprice), ...);
     * </pre>
     * If all values in the array are zero, zero is returned.
     */
    @ReplacedBy("com.google.common.collect.ComparisonChain")
    public static int combine (int ... values)
    {
        for (int value : values) {
            if (value != 0) {
                return value;
            }
        }
        return 0;
    }

    // Double.compare() exists

    // Float.compare() exists

    /**
     * Compares two booleans, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Boolean.
     */
    @ReplacedBy("com.google.common.primitives.Booleans.compare()")
    public static int compare (boolean value1, boolean value2)
    {
        return (value1 == value2) ? 0 : (value1 ? 1 : -1);
    }
}
