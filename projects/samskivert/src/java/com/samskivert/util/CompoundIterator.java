//
// $Id: CompoundIterator.java,v 1.2 2001/11/26 19:21:33 mdb Exp $
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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over a collection of iterators, moving seamlessly from one
 * iterator to the next as the first runs out of elements. Iterators are
 * made available to the compound iterator via the succinct and functional
 * {@link IteratorProvider} interface.
 */
public class CompoundIterator implements Iterator
{
    /** Used to obtain the set of iterators over which we will iterate. */
    public static interface IteratorProvider
    {
        /**
         * Returns the next iterator in the set. Should return null when
         * we've run out of iterators.
         */
        public Iterator nextIterator ();
    }

    /**
     * Constructs a compound iterator that will iterate over all of the
     * iterators provided by the supplied provider, in turn.
     */
    public CompoundIterator (IteratorProvider provider)
    {
        _provider = provider;
        // move to the first iterator
        _iter = _provider.nextIterator();
    }

    // documentation inherited
    public boolean hasNext ()
    {
        // keep trying iterators until we run out or find an element
        while (_iter != null) {
            if (_iter.hasNext()) {
                return true;
            } else {
                _iter = _provider.nextIterator();
            }
        }

        return false;
    }

    // documentation inherited
    public Object next ()
    {
        // keep trying iterators until we run out or find an element
        while (_iter != null) {
            try {
                return _iter.next();
            } catch (NoSuchElementException nsee) {
                // have to catch this and move to the next iterator
                _iter = _provider.nextIterator();
            }
        }

        throw new NoSuchElementException();
    }

    // documentation inherited
    public void remove ()
    {
        if (_iter != null) {
            _iter.remove();
        } else {
            throw new IllegalStateException();
        }
    }

    /** Our provider of iterators. */
    protected IteratorProvider _provider;

    /** The iterator currently in use. */
    protected Iterator _iter;
}
