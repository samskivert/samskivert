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
        if (!(other instanceof Tuple)) {
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
