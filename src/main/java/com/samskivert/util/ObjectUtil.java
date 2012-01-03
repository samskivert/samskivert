//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.lang.reflect.Field;
import java.io.PrintStream;

import java.util.Arrays;

import com.samskivert.annotation.*;

/**
 * Utility methods that don't fit anywhere else.
 */
public class ObjectUtil
{
    /**
     * Cast the specified Object, or return null if it is not an instance.
     */
    public static <T> T as (Object obj, Class<T> clazz)
    {
        return clazz.isInstance(obj) ? clazz.cast(obj) : null;
    }

    /**
     * Test two objects for equality safely.
     */
    @ReplacedBy("com.google.common.base.Objects#equal(Object,Object)")
    public static boolean equals (Object o1, Object o2)
    {
        return (o1 == o2) || ((o1 != null) && o1.equals(o2));
    }

    /**
     * Returns true if the two supplied exceptions have equal messages and
     * equal stack traces.
     */
    public static boolean equals (Throwable t1, Throwable t2)
    {
        return (t1 == t2) ||
            ((t1 != null) && (t2 != null) &&
                equals(t1.getMessage(), t2.getMessage()) &&
                Arrays.equals(t1.getStackTrace(), t2.getStackTrace()));
    }

    /**
     * Compares two objects, returning -1 if left is null and right is not and 1 if left is
     * non-null and right is null.
     */
    @ReplacedBy("com.google.common.collect.Ordering.natural().nullsFirst().compare()")
    public static <T extends Comparable<? super T>> int compareTo (T left, T right)
    {
        if (left == right) { // instances are both null or both the same instance
            return 0;
        } else if (left == null) {
            return -1;
        } else if (right == null) {
            return 1;
        } else {
            return left.compareTo(right);
        }
    }

    /**
     * Dumps the contents of the supplied object instance, listing the
     * class name, hash code and <code>toString()</code> data for each
     * field in the supplied object and then dumps each field in turn.
     */
    public static void dumpInstance (PrintStream out, Object object)
    {
        dumpInstance(out, object, true);
    }

    /**
     * Dumps the contents of the supplied object instance, listing the
     * class name, hash code and optionally, the <code>toString()</code>
     * data for each field in the supplied object and then dumps each
     * field in turn.
     */
    public static void dumpInstance (PrintStream out, Object object,
                                     boolean includeToString)
    {
        dumpInstance(out, object, includeToString, "");
    }

    /**
     * Helper function.
     */
    protected static void dumpInstance (
        PrintStream out, Object object, boolean includeToString, String indent)
    {
        // summarize the current object
        summarizeObject(out, object, includeToString, indent);
        // increment the indentation level
        indent += " ";
        // summarize the object's fields
        Field[] fields = object.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                Object field = fields[i].get(object);
                dumpInstance(out, field, includeToString, indent);
            } catch (Exception e) {
                out.println(indent + "*** unable to fetch field " +
                            "[field=" + fields[i] + ", error=" + e + "]");
            }
        }
    }

    /**
     * Prints out the object class name, hash code and optionally the
     * <code>toString()</code> data for the supplied object.
     *
     * @param out the print stream on which to dump the object.
     * @param object the object to be dumped.
     * @param includeToString true if the result of {@link
     * Object#toString} should be included in the output.
     * @param prefix a prefix to prepend to the output.
     */
    public static void summarizeObject (
        PrintStream out, Object object, boolean includeToString, String prefix)
    {
        out.print(prefix + object.getClass().getName());
        out.print(" [" + object.hashCode());
        if (includeToString) {
            out.print(", " + object.toString());
        }
        out.println("]");
    }
}
