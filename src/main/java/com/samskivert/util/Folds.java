//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Various useful folds over iterables.
 */
public class Folds
{
    /** Used with {@link #foldLeft}. */
    public interface F<B,A>
    {
        /** Folds the supplied element into the result. */
        B apply (B zero, A elem);
    }

    /** For reductions and same-typed folds. */
    public interface R<A> extends F<A,A> {}

    /**
     * Left folds the supplied function over the supplied values using the supplied starting value.
     */
    public static <A, B> B foldLeft (F<B,A> func, B zero, Iterable<? extends A> values)
    {
        for (A value : values) {
            zero = func.apply(zero, value);
        }
        return zero;
    }

    /**
     * Reduces the supplied values using the supplied function with a left fold.
     *
     * @exception NoSuchElementException thrown if values does not contain at least one element.
     */
    public static <A> A reduceLeft (F<A,A> func, Iterable<? extends A> values)
    {
        Iterator<? extends A> iter = values.iterator();
        A zero = iter.next();
        while (iter.hasNext()) {
            zero = func.apply(zero, iter.next());
        }
        return zero;
    }

    /**
     * Sums the supplied collection of numbers to an int with a left to right fold.
     */
    public static int sum (int zero, Iterable<? extends Number> values)
    {
        for (Number value : values) {
            zero += value.intValue();
        }
        return zero;
    }

    /**
     * Sums the supplied collection of numbers to a long with a left to right fold.
     */
    public static long sum (long zero, Iterable<? extends Number> values)
    {
        for (Number value : values) {
            zero += value.longValue();
        }
        return zero;
    }

    /**
     * Sums the supplied collection of numbers to a float with a left to right fold.
     */
    public static float sum (float zero, Iterable<? extends Number> values)
    {
        for (Number value : values) {
            zero += value.floatValue();
        }
        return zero;
    }

    /**
     * Sums the supplied collection of numbers to a double with a left to right fold.
     */
    public static double sum (double zero, Iterable<? extends Number> values)
    {
        for (Number value : values) {
            zero += value.doubleValue();
        }
        return zero;
    }

    /**
     * Sums the supplied collection of numbers to a bigint with a left to right fold.
     */
    public static BigInteger sum (BigInteger zero, Iterable<? extends BigInteger> values)
    {
        for (BigInteger value : values) {
            zero = zero.add(value);
        }
        return zero;
    }
}
