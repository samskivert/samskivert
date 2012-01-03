//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.Serializable;

/**
 * A triple of values that properly implements {@link #hashCode} and {@link #equals}.
 */
public class Triple<A,B,C> implements Serializable
{
    /** The first object. */
    public final A a;

    /** The second object. */
    public final B b;

    /** The third object. */
    public final C c;

    /**
     * Creates a triple with the specified values.
     */
    public static <A, B, C> Triple<A, B, C> newTriple (A a, B b, C c)
    {
        return new Triple<A, B, C>(a, b, c);
    }

    /**
     * Constructs a triple with the specified two objects.
     */
    public Triple (A a, B b, C c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override // from Object
    public int hashCode ()
    {
        int value = 17;
        value = value * 31 + ((a == null) ? 0 : a.hashCode());
        value = value * 31 + ((b == null) ? 0 : b.hashCode());
        value = value * 31 + ((c == null) ? 0 : c.hashCode());
        return value;
    }

    @Override // from Object
    public boolean equals (Object other)
    {
        if (!(other instanceof Triple<?, ?, ?>)) {
            return false;
        }
        Triple<?, ?, ?> to = (Triple<?, ?, ?>)other;
        return ObjectUtil.equals(a, to.a) && ObjectUtil.equals(b, to.b) &&
            ObjectUtil.equals(c, to.c);
    }

    @Override // from Object
    public String toString ()
    {
        return "[a=" + a + ", b=" + b + ", c=" + c + "]";
    }

    /** Don't you go a changin'. */
    private static final long serialVersionUID = 1;
}
