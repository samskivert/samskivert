//
// $Id: DataTool.java,v 1.3 2004/01/05 19:13:30 eric Exp $

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
    public List sort (List list)
    {
        Collections.sort(list);
        return list;
    }

    /**
     * Copies the data from the supplied collection into a list, sorts it and
     * returns the list. The elements <em>must</em> implement {@link
     * Comparable}.
     */
    public List sort (Collection data)
    {
        ArrayList list = new ArrayList(data);
        Collections.sort(list);
        return list;
    }

    /**
     * Copies the data from the supplied iterator into a list, sorts it and
     * returns the list. The elements <em>must</em> implement {@link
     * Comparable}.
     */
    public List sort (Iterator iter)
    {
        ArrayList list = new ArrayList();
        CollectionUtil.addAll(list, iter);
        Collections.sort(list);
        return list;
    }
}
