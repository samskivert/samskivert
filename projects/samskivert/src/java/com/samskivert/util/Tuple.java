//
// $Id: Tuple.java,v 1.2 2001/08/11 22:43:29 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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
 * A tuple is a simple object that holds a reference to two other objects.
 */
public class Tuple
{
    /** The left object. */
    public Object left;

    /** The right object. */
    public Object right;

    /** Construct a tuple with the specified two objects. */
    public Tuple (Object left, Object right)
    {
        this.left = left;
        this.right = right;
    }

    /** Construct a blank tuple. */
    public Tuple ()
    {
    }

    public String toString ()
    {
        return "[left=" + left + ", right=" + right + "]";
    }
}
