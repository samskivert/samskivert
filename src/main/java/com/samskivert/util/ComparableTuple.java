//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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
