//
// $Id$

package com.samskivert.util;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A more memory-efficient Set implementation than HashSet.
 */
public class ArraySet<E> extends AbstractSet<E>
{
    public ArraySet ()
    {
        this(DEFAULT_CAPACITY);
    }

    public ArraySet (int initialCapacity)
    {
        _hashCodes = new int[initialCapacity];
        _elements = new Object[initialCapacity];
    }

    public ArraySet (Collection<? extends E> c)
    {
        this(c.size());
        addAll(c);
    }

    @Override
    public boolean contains (Object o)
    {
        o = wrap(o);
        return (indexFor(o.hashCode(), o) >= 0);
    }

    @Override
    public boolean add (E e)
    {
        Object o = wrap(e);
        int hashCode = o.hashCode();
        int index = indexFor(hashCode, o);
        if (index >= 0) {
            return false; // we've already got one!
        }
        // convert the index to the actual insertion point
        index = -(index + 1);

        // possibly grow the arrays
        int len = _hashCodes.length;
        int[] codes = _hashCodes;
        Object[] elems = _elements;
        if (len == _size) {
            int newLen = (len >= Integer.MAX_VALUE/2)
                ? Integer.MAX_VALUE
                : Math.max(DEFAULT_CAPACITY, len * 2);
            _hashCodes = new int[newLen];
            System.arraycopy(codes, 0, _hashCodes, 0, index);
            _elements = new Object[newLen];
            System.arraycopy(elems, 0, _elements, 0, index);
        }

        // shift and insert
        if (_size > index) {
            System.arraycopy(codes, index, _hashCodes, index + 1, _size - index);
            System.arraycopy(elems, index, _elements, index + 1, _size - index);
        }

        _hashCodes[index] = hashCode;
        _elements[index] = o;
        _size++;
        return true;
    }

    @Override
    public boolean remove (Object o)
    {
        o = wrap(o);
        int hashCode = o.hashCode();
        int index = indexFor(hashCode, o);
        if (index < 0) {
            return false; // not found
        }
        removeAtIndex(index);
        return true;
    }

    @Override
    public int size ()
    {
        return _size;
    }

    @Override
    public Iterator<E> iterator ()
    {
        // TODO: comodification
        return new Iterator<E>() {
            public boolean hasNext ()
            {
                return (_cursor < _size);
            }

            public E next ()
            {
                if (_cursor >= _size) {
                    throw new NoSuchElementException();
                }
                _canRemove = true;
                return unwrap(_elements[_cursor++]);
            }

            public void remove ()
            {
                if (!_canRemove) {
                    throw new IllegalStateException();
                }
                --_cursor;
                removeAtIndex(_cursor);
                _canRemove = false;
            }

            protected int _cursor;
            protected boolean _canRemove;
        };
    }

    @Override
    public void clear ()
    {
        _hashCodes = new int[DEFAULT_CAPACITY];
        _elements = new Object[DEFAULT_CAPACITY];
        _size = 0;
    }

    protected int indexFor (int hashCode, Object o)
    {
        int index = Arrays.binarySearch(_hashCodes, 0, _size, hashCode);
        if (index < 0) {
            return index; // not found, so return our insertion point
        }
        // see if we landed right on it
        if (o.equals(_elements[index])) {
            return index; // found it!
        }
        // search forward for it
        for (int ii = index + 1; (ii < _size) && (_hashCodes[ii] == hashCode); ii++) {
            if (o.equals(_elements[ii])) {
                return ii;
            }
        }
        // search backward for it
        for (int ii = index - 1; (ii >= 0) && (_hashCodes[ii] == hashCode); ii--) {
            if (o.equals(_elements[ii])) {
                return ii;
            }
        }
        // else convert our original found index into an insertion point
        return -(index + 1);
    }

    protected void removeAtIndex (int index)
    {
        _size--;
        int len = _hashCodes.length;
        if ((len > DEFAULT_CAPACITY) && (_size < len/8)) {
            // we're using less than 1/8 of our capacity, shrink by half, omitting the removed elem
            int[] codes = new int[len / 2];
            System.arraycopy(_hashCodes, 0, codes, 0, index);
            System.arraycopy(_hashCodes, index + 1, codes, index,  _size - index);
            _hashCodes = codes;
            Object[] elems = new Object[len / 2];
            System.arraycopy(_elements, 0, elems, 0, index);
            System.arraycopy(_elements, index + 1, elems, index, _size - index);
            _elements = elems;

        } else {
            // otherwise, simply shift the elements above it downward
            System.arraycopy(_hashCodes, index + 1, _hashCodes, index, _size - index);
            System.arraycopy(_elements, index + 1, _elements, index, _size - index);
            _elements[_size] = null; // clear loose refs, but no need to clean hashCodes
        }
    }

    protected Object wrap (Object o)
    {
        return ((o == null) ? NULL : o);
    }

    @SuppressWarnings("unchecked")
    protected E unwrap (Object o)
    {
        return ((o == NULL) ? null : (E)o);
    }

    protected int _size;
    protected int[] _hashCodes;
    protected Object[] _elements;

    protected static final Object NULL = new Object();

    protected static int DEFAULT_CAPACITY = 8;
}
