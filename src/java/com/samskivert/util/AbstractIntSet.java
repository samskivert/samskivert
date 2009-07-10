//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2008 Michael Bayne
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

import java.util.AbstractSet;
import java.util.Iterator;

/**
 * A base class for {@link IntSet} implementations.
 */
public abstract class AbstractIntSet extends AbstractSet<Integer>
    implements IntSet
{
    @Override // from AbstractSet
    public Iterator<Integer> iterator ()
    {
        return interator();
    }

    @Override // from AbstractSet
    public boolean add (int t)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an iterator that provides access to our int values without unboxing.
     */
    public abstract Interator interator ();

    /**
     * Converts the contents of this set to an int array.
     */
    public int[] toIntArray ()
    {
        int[] vals = new int[size()];
        int ii=0;
        for (Interator intr = interator(); intr.hasNext(); ) {
            vals[ii++] = intr.nextInt();
        }
        return vals;
    }
}
