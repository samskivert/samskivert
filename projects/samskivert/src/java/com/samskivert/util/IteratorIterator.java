//
// $Id: IteratorIterator.java,v 1.1 2002/11/07 05:58:31 mdb Exp $

package com.samskivert.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that can iterate over the elements in several collections
 * contained within a master collection.
 */
public class IteratorIterator implements Iterator
{
    /**
     * @param collections a Collection containing more Collections
     * whose elements we are to iterate over.
     */
    public IteratorIterator (Collection collections)
    {
        _meta = collections.iterator();
    }

    // documentation inherited from interface Iterator
    public boolean hasNext ()
    {
        while ((_current == null) || (!_current.hasNext())) {
            if (_meta.hasNext()) {
                _current = ((Collection) _meta.next()).iterator();
            } else {
                return false;
            }
        }
        return true;
    }

    // documentation inherited from interface Iterator
    public Object next ()
    {
        if (hasNext()) {
            return _current.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    // documentation inherited from interface Iterator
    public void remove ()
    {
        if (_current != null) {
            _current.remove();
        } else {
            throw new IllegalStateException();
        }
    }

    /** The iterator through the collection we were constructed with. */
    protected Iterator _meta;

    /** The current sub-collection's iterator. */
    protected Iterator _current;
}
