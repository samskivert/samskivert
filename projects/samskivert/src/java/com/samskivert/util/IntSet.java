//
// $Id: IntSet.java,v 1.1 2002/02/03 07:10:16 mdb Exp $
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

import java.util.Set;

/**
 * A set that holds integers and provides accessors that eliminate the
 * need to create and manipulate superfluous <code>Integer</code>
 * objects. It extends the <code>Set</code> interface and therefore
 * provides all of the standard methods (in which <code>Integer</code>
 * objects will be converted to ints).
 */
public interface IntSet extends Set
{
    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param value element whose presence in this set is to be tested.
     *
     * @return <tt>true</tt> if this set contains the specified element.
     */
    public boolean contains (int value);

    /**
     * Adds the specified element to this set if it is not already present
     * (optional operation). If this set already contains the specified
     * element, the call leaves this set unchanged and returns
     * <tt>false</tt>.  In combination with the restriction on
     * constructors, this ensures that sets never contain duplicate
     * elements.
     *
     * @param value element to be added to this set.
     *
     * @return <tt>true</tt> if this set did not already contain the
     * specified element.
     * 
     * @throws UnsupportedOperationException if the <tt>add</tt> method is
     * not supported by this set.
     */
    public boolean add (int value);

    /**
     * Removes the specified element from this set if it is present
     * (optional operation).  Returns <tt>true</tt> if the set contained
     * the specified element (or equivalently, if the set changed as a
     * result of the call).  (The set will not contain the specified
     * element once the call returns.)
     *
     * @param value element to be removed from this set, if present.
     *
     * @return true if the set contained the specified element.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt> method
     * is not supported by this set.
     */
    public boolean remove (int value);

    /**
     * Returns an array containing all of the elements in this set.  Obeys
     * the general contract of the <tt>Collection.toArray</tt> method.
     *
     * @return an array containing all of the elements in this set.
     */
    public int[] toIntArray ();
}
