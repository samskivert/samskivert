//
// $Id: SortedIterator.java,v 1.1 2003/12/17 21:58:06 ray Exp $

package com.samskivert.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * An iterator that returns the elements from another iterator into
 * some sorted order.
 */
public class SortedIterator implements Iterator
{
    /**
     * Construct a sorted iterator that iterates through the specified
     * elements in natural order.
     */
    public SortedIterator (Iterator itr)
    {
        this(itr, Comparators.COMPARABLE);
    }

    /**
     * Construct a sorted iterator that iterates through the specified
     * elements in the order according to the specified comparator.
     */
    public SortedIterator (Iterator itr, Comparator comparator)
    {
        SortableArrayList list = new SortableArrayList();
        while (itr.hasNext()) {
            list.insertSorted(itr.next(), comparator);
        }
        _itr = list.iterator();
    }

    /**
     * Construct a sorted iterator that iterates through the specified
     * collection in natural order.
     */
    public SortedIterator (Collection c)
    {
        this(c.iterator());
    }

    /**
     * Construct a sorted iterator that iterates through the specified
     * collection in the order according to the specified comparator.
     */
    public SortedIterator (Collection c, Comparator comparator)
    {
        this(c.iterator(), comparator);
    }

    // documentation inherited from interface Iterator
    public boolean hasNext ()
    {
        return _itr.hasNext();
    }

    // documentation inherited from interface Iterator
    public Object next ()
    {
        return _itr.next();
    }

    // documentation inherited from interface Iterator
    public void remove ()
    {
        // sadly, we cannot
        throw new UnsupportedOperationException(
            "Cannot remove from a SortedIterator");
    }

    /** The iterator we are a wrapper around. */
    protected Iterator _itr;
}
