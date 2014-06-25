//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.Serializable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.RandomAccess;

import java.lang.reflect.Array;

/**
 * Provides a base for extending the standard Java {@link ArrayList}
 * functionality (which we'd just extend directly if those pig fuckers hadn't
 * made the instance variables private).
 *
 * <p><em>Note:</em> Does not support null elements.</p>
 */
public abstract class BaseArrayList<E> extends AbstractList<E>
    implements RandomAccess, Cloneable, Serializable
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

        } else if (target.length > _size) {
            // terminate with null if there is room to spare, per the spec
            target[_size] = null;
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
        modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean add (E o)
    {
        _elements = (E[])ListUtil.add(_elements, _size, o);
        _size++;
        modCount++;
        return true;
    }

    @Override
    public boolean remove (Object o)
    {
        if (ListUtil.remove(_elements, o) != null) {
            _size--;
            modCount++;
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
        // do not increment modCount: this is not a structural modification
        return old;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void add (int index, E element)
    {
        rangeCheck(index, true);
        _elements = (E[])ListUtil.insert(_elements, index, element);
        _size++;
        modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E remove (int index)
    {
        rangeCheck(index, false);
        E oval = (E)ListUtil.remove(_elements, index);
        _size--;
        modCount++;
        return oval;
    }

    @Override
    public int indexOf (Object o)
    {
        return ListUtil.indexOf(_elements, o); // non-optimal: will check indexes >= _size
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
    public BaseArrayList<E> clone ()
    {
        try {
            @SuppressWarnings("unchecked")
            BaseArrayList<E> dup = (BaseArrayList<E>)super.clone();
            if (_elements != null) {
                dup._elements = _elements.clone();
            }
            return dup;

        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse); // won't happen; we are Cloneable
        }
    }

    /** The array of elements in our list. */
    protected E[] _elements;

    /** The number of elements in our list. */
    protected int _size;

    /** Avoid serialization annoyance. */
    private static final long serialVersionUID = 1;
}
