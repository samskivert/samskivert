//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import java.util.Comparator;

/**
 * A repository for standard comparators.
 */
public class Comparators
{
    /**
     * A comparator that compares the toString() value of all objects case insensitively.
     */
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
            return String.CASE_INSENSITIVE_ORDER.compare(
                o1.toString(), o2.toString());
        }
    };

    /**
     * A comparator that compares {@link Comparable} instances.
     */
    public static final Comparator COMPARABLE = new Comparator() {
        @SuppressWarnings("unchecked")
        public int compare (Object o1, Object o2)
        {
            if (o1 == o2) { // catches null == null
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }
            return ((Comparable)o1).compareTo(o2); // null-free
        }
    };

    /**
     * Returns the Comparator for Comparables, properly cast.
     *
     * <p>This example illustates the type-safe way to obtain a natural-ordering Comparator:
     * <pre>
     *    Comparator&lt;Integer&gt; = Comparators.comparable();
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static final <T extends Comparable> Comparator<T> comparable ()
    {
        return (Comparator<T>) COMPARABLE;
    }

    /**
     * Compares two bytes, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Byte.
     */
    public static int compare (byte value1, byte value2)
    {
	return (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));
    }

    /**
     * Compares two chars, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Character.
     */
    public static int compare (char value1, char value2)
    {
	return (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));
    }

    /**
     * Compares two shorts, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Character.
     */
    public static int compare (short value1, short value2)
    {
	return (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));
    }

    /**
     * Compares two integers in an overflow safe manner, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Integer.
     */
    public static int compare (int value1, int value2)
    {
	return (value1 < value2 ? -1 : (value1 == value2 ? 0 : 1));
    }

    /**
     * Compares two longs in an overflow safe manner, returning 1, 0, or -1.
     * TODO: remove when Java finally has this method in Long.
     */
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
    public static int compare (boolean value1, boolean value2)
    {
        return (value1 == value2) ? 0 : (value1 ? 1 : -1);
    }
}
