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

package com.samskivert.velocity;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.CollectionUtil;

/**
 * Some helpful methods for dealing with data in velocity.
 */
public class DataTool
{
    /**
     * Returns the object at location zero in the array, super useful for
     * those length 1 arrays (like when you are storing primitives in a
     * hash)
     */
    public Object unwrap (Object array)
    {
        return get(array, 0);
    }

    /**
     * Returns the object in the array at index.  Wrapped as an object if
     * it is a primitive.
     */
    public Object get (Object array, int index)
    {
        return (array == null) ? null : Array.get(array, index);
    }

    /**
     * Returns the length of the specified array.
     */
    public int length (Object array)
    {
        return (array == null) ? 0 : Array.getLength(array);
    }

    /**
     * Returns the numerator as a percentage of the denominator (100 * num /
     * denom), handles 0/0 (returns 0), but will div0 on N/0 where N != 0.
     */
    public int percent (int num, int denom)
    {
        return (num == 0) ? 0 : 100 * num / denom;
    }

    /**
     * Floating point divide.
     */
    public float div (float a, float b)
    {
        return a/b;
    }

    /**
     * Double addition.
     */
    public double add (double a, double b)
    {
        return a + b;
    }

    /**
     * Sorts the supplied list and returns it. The elements <em>must</em>
     * implement {@link Comparable}.
     */
    public <T extends Comparable<? super T>> List<T> sort (List<T> list)
    {
        Collections.sort(list);
        return list;
    }

    /**
     * Copies the data from the supplied collection into a list, sorts it and
     * returns the list. The elements <em>must</em> implement {@link
     * Comparable}.
     */
    public <T extends Comparable<? super T>> List<T> sort (Collection<T> data)
    {
        return sort(new ArrayList<T>(data));
    }

    /**
     * Copies the data from the supplied iterator into a list, sorts it and
     * returns the list. The elements <em>must</em> implement {@link
     * Comparable}.
     */
    public <T extends Comparable<? super T>> List<T> sort (Iterator<T> iter)
    {
        ArrayList<T> list = new ArrayList<T>();
        CollectionUtil.addAll(list, iter);
        return sort(list);
    }
}
