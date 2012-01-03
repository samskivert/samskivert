//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * A pair of {@link Comparable} objects that is itself {@link Comparable}.
 */
public class ComparableTuple<L extends Comparable<? super L>, R extends Comparable<? super R>>
    extends Tuple<L,R>
    implements Comparable<ComparableTuple<L,R>>
{
    /**
     * Constructs a tuple with the supplied contents.
     */
    public ComparableTuple (L left, R right)
    {
        super(left, right);
    }

    // from interface Comparable
    public int compareTo (ComparableTuple<L, R> other)
    {
        int rv = ObjectUtil.compareTo(left, other.left);
        return (rv != 0) ? rv : ObjectUtil.compareTo(right, other.right);
    }
}
