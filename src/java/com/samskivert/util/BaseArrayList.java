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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

import java.lang.reflect.Array;

import static com.samskivert.Log.log;

/**
 * Provides a base for extending the standard Java {@link ArrayList}
 * functionality (which we'd just extend directly if those pig fuckers hadn't
 * made the instance variables private).
 */
public abstract class BaseArrayList<E> extends AbstractList<E>
    implements List<E>, RandomAccess, Cloneable, Serializable
{
    @Override
    public int size ()
    {
        return _size;
    }

    @Override
    public boolean isEmpty ()
    {
        return (_size == 0);
    }

    @Override
    public boolean contains (Object o)
    {
        return ListUtil.contains(_elements, o);
    }

    @Override
    public Object[] toArray ()
    {
        return toArray(new Object[_size]);
    }

    @Override
    @SuppressWarnings("unchecked")
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

    @Override
    public void clear ()
    {
        _elements = null;
        _size = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean add (E o)
    {
        _elements = (E[])ListUtil.add(_elements, _size, o);
        _size++;
        return true;
    }

    @Override
    public boolean remove (Object o)
    {
        if (ListUtil.remove(_elements, o) != null) {
            _size--;
            return true;
        }
        return false;
    }

    @Override
    public E get (int index)
    {
        rangeCheck(index, false);
        return _elements[index];
    }

    @Override
    public E set (int index, E element)
    {
        rangeCheck(index, false);
        E old = _elements[index];
        _elements[index] = element;
        return old;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void add (int index, E element)
    {
        rangeCheck(index, true);
        _elements = (E[])ListUtil.insert(_elements, index, element);
        _size++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E remove (int index)
    {
        rangeCheck(index, false);
        E oval = (E)ListUtil.remove(_elements, index);
        _size--;
        return oval;
    }

    @Override
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

    @Override
    @SuppressWarnings("unchecked")
    public Object clone ()
    {
        try {
            BaseArrayList<E> dup = (BaseArrayList<E>)super.clone();
            if (_elements != null) {
                dup._elements = _elements.clone();
            }
            return dup;

        } catch (CloneNotSupportedException cnse) {
            log.warning("clone failed", cnse); // won't happen.
            return null;
        }
    }

    /** The array of elements in our list. */
    protected E[] _elements;

    /** The number of elements in our list. */
    protected int _size;

    /** Avoid serialization annoyance. */
    private static final long serialVersionUID = 1;
}
