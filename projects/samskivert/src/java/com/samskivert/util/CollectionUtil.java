//
// $Id: CollectionUtil.java,v 1.3 2001/09/27 23:13:24 mdb Exp $
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

import java.util.*;

/**
 * A collection of collection-related utility functions.
 */
public class CollectionUtil
{
    /**
     * Adds all items returned by the enumeration to the supplied
     * collection.
     */
    public static void addAll (Collection col, Enumeration enum)
    {
        while (enum.hasMoreElements()) {
            col.add(enum.nextElement());
        }
    }

    /**
     * Adds all items returned by the iterator to the supplied collection.
     */
    public static void addAll (Collection col, Iterator iter)
    {
        while (iter.hasNext()) {
            col.add(iter.next());
        }
    }

    /**
     * If a collection contains only <code>Integer</code> objects, it can
     * be passed to this function and converted into an int array.
     *
     * @param col the collection to be converted.
     *
     * @return an int array containing the contents of the collection (in
     * the order returned by the collection's iterator). The size of the
     * array will be equal to the size of the collection.
     *
     * @exception ClassCastException thrown if the collection contains
     * elements that are not instances of <code>Integer</code>.
     */
    public static int[] toIntArray (Collection col)
    {
        Iterator iter = col.iterator();
        int[] array = new int[col.size()];
        for (int i = 0; iter.hasNext(); i++) {
            array[i] = ((Integer)iter.next()).intValue();
        }
        return array;
    }
}
