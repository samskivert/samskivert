//
// $Id: BaseArrayList.java,v 1.21 2004/02/25 13:20:44 mdb Exp $
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

import java.io.Serializable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;

import java.lang.reflect.Array;

/**
 * Provides a base for extending the standard Java {@link ArrayList}
 * functionality (which we'd just extend directly if those pig fuckers hadn't
 * made the instance variables private).
 */
public abstract class BaseArrayList<T> extends AbstractList<T>
    implements List<T>, RandomAccess, Cloneable, Serializable
{
    // documentation inherited from interface
    public int size ()
    {
        return _size;
    }

    // documentation inherited from interface
    public boolean isEmpty ()
    {
        return (_size == 0);
    }

    // documentation inherited from interface
    public boolean contains (Object o)
    {
        return ListUtil.contains(_elements, o);
    }

    // documentation inherited from interface
    public Object[] toArray ()
    {
        return toArray(new Object[_size]);
    }

    @SuppressWarnings("unchecked") // documentation inherited from interface
    public <T> T[] toArray (T[] target)
    {
        // create the target array if necessary
        if (target.length < _size) {
            target = (T[])Array.newInstance(
                target.getClass().getComponentType(), _size);
        }

        // copy the elements
        if (_elements != null) {
            System.arraycopy(_elements, 0, target, 0, _size);
        }

        return target;
    }

    // documentation inherited
    public void clear ()
    {
        _elements = null;
        _size = 0;
    }

    @SuppressWarnings("unchecked") // documentation inherited from interface
    public boolean add (T o)
    {
        _elements = (T[])ListUtil.add(_elements, _size, o);
        _size++;
        return true;
    }

    // documentation inherited from interface
    public boolean remove (Object o)
    {
        if (ListUtil.remove(_elements, o) != null) {
            _size--;
            return true;
        }
        return false;
    }

    // documentation inherited from interface
    public T get (int index)
    {
        rangeCheck(index, false);
        return _elements[index];
    }

    // documentation inherited from interface
    public T set (int index, T element)
    {
        rangeCheck(index, false);
        T old = _elements[index];
        _elements[index] = element;
        return old;
    }

    @SuppressWarnings("unchecked") // documentation inherited from interface
    public void add (int index, T element)
    {
        rangeCheck(index, true);
        _elements = (T[])ListUtil.insert(_elements, index, element);
        _size++;
    }

    @SuppressWarnings("unchecked") // documentation inherited from interface
    public T remove (int index)
    {
        rangeCheck(index, false);
        T oval = (T)ListUtil.remove(_elements, index);
        _size--;
        return oval;
    }

    // documentation inherited from interface
    public int indexOf (Object o)
    {
        return ListUtil.indexOf(_elements, o);
    }

//     // documentation inherited from interface
//     public int lastIndexOf (Object o)
//     {
//     }

    /**
     * Check the range of a passed-in index to make sure it's valid.
     *
     * @param insert if true, an index equal to our size is valid.
     */
    protected final void rangeCheck (int index, boolean insert)
    {
        if ((index < 0) || (insert ? (index > _size) : (index >= _size))) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + _size);
        }
    }

    @SuppressWarnings("unchecked") // documentation inherited
    public Object clone ()
    {
        try {
            BaseArrayList<T> dup = (BaseArrayList<T>)super.clone();
            if (_elements != null) {
                dup._elements = (T[])_elements.clone();
            }
            return dup;

        } catch (CloneNotSupportedException cnse) {
            com.samskivert.Log.logStackTrace(cnse); // won't happen.
            return null;
        }
    }

    /** The array of elements in our list. */
    protected T[] _elements;

    /** The number of elements in our list. */
    protected int _size;

    /** Avoid serialization annoyance. */
    private static final long serialVersionUID = 1;
}
