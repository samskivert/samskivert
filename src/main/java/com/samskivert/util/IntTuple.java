//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.Serializable;

/**
 * A simple object that holds a reference to two ints.
 */
public class IntTuple
    implements Comparable<IntTuple>, Serializable
{
    /** The left int. */
    public int left;

    /** The right int. */
    public int right;

    /** Construct a tuple with the specified two objects. */
    public IntTuple (int left, int right)
    {
        this.left = left;
        this.right = right;
    }

    /** Construct a blank tuple. */
    public IntTuple ()
    {
    }

    /**
     * Returns the combined hashcode of the two elements.
     */
    @Override
    public int hashCode ()
    {
        return left ^ right;
    }

    // documentation inherited from interface
    public int compareTo (IntTuple ot)
    {
        return (left == ot.left) ? (right - ot.right) : (left - ot.left);
    }

    /**
     * A tuple is equal to another tuple if the left and right elements
     * are equal to the left and right elements (respectively) of the
     * other tuple.
     */
    @Override
    public boolean equals (Object other)
    {
        if (other instanceof IntTuple) {
            IntTuple to = (IntTuple)other;
            return (left == to.left && right == to.right);
        } else {
            return false;
        }
    }

    @Override
    public String toString ()
    {
        return "[left=" + left + ", right=" + right + "]";
    }
}
