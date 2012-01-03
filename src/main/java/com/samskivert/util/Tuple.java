//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.Serializable;

/**
 * A tuple is a simple object that holds a reference to two other objects. It provides hashcode and
 * equality semantics that allow it to be used to combine two objects into a single key (for
 * hashtables, etc.).
 */
public class Tuple<L,R> implements Serializable
{
    /** The left object. */
    public final L left;

    /** The right object. */
    public final R right;

    /**
     * Creates a tuple with the specified two objects.
     */
    public static <L, R> Tuple<L, R> newTuple (L left, R right)
    {
        return new Tuple<L, R>(left, right);
    }

    /**
     * Constructs a tuple with the supplied values.
     */
    public Tuple (L left, R right)
    {
        this.left = left;
        this.right = right;
    }

    @Override // from Object
    public int hashCode ()
    {
        int value = 17;
        value = value * 31 + ((left == null) ? 0 : left.hashCode());
        value = value * 31 + ((right == null) ? 0 : right.hashCode());
        return value;
    }

    @Override // from Object
    public boolean equals (Object other)
    {
        if (!(other instanceof Tuple<?, ?>)) {
            return false;
        }
        Tuple<?, ?> to = (Tuple<?, ?>)other;
        return ObjectUtil.equals(left, to.left) && ObjectUtil.equals(right, to.right);
    }

    @Override // from Object
    public String toString ()
    {
        return "[left=" + left + ", right=" + right + "]";
    }

    /** Don't you go a changin'. */
    private static final long serialVersionUID = 1;
}
