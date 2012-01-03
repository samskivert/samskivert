//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
