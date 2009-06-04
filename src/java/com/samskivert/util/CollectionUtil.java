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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of collection-related utility functions.
 */
public class CollectionUtil
{
    /**
     * Adds all items returned by the enumeration to the supplied collection
     * and returns the supplied collection.
     */
    public static <E, T extends Collection<E>> T addAll (
            T col, Enumeration<E> enm)
    {
        while (enm.hasMoreElements()) {
            col.add(enm.nextElement());
        }
        return col;
    }

    /**
     * Adds all items returned by the iterator to the supplied collection and
     * returns the supplied collection.
     */
    public static <E, T extends Collection<E>> T addAll (
            T col, Iterator<E> iter)
    {
        while (iter.hasNext()) {
            col.add(iter.next());
        }
        return col;
    }

    /**
     * Adds all items in the given object array to the supplied collection and
     * returns the supplied collection. If the supplied array is null, nothing
     * is added to the collection.
     */
    public static <E, T extends Collection<E>> T addAll (T col, E[] values)
    {
        if (values != null) {
            for (int ii = 0; ii < values.length; ii++) {
                col.add(values[ii]);
            }
        }
        return col;
    }

    /**
     * Returns a list containing a random selection of elements from the
     * specified collection. The total number of elements selected will be
     * equal to <code>count</code>, each element in the source collection will
     * be selected with equal probability and no element will be included more
     * than once. The elements in the random subset will always be included in
     * the order they are returned from the source collection's iterator.
     *
     * <p> Algorithm courtesy of William R. Mahoney, published in
     * <cite>Dr. Dobbs Journal, February 2002</cite>.
     *
     * @exception IllegalArgumentException thrown if the size of the collection
     * is smaller than the number of elements requested for the random subset.
     */
    public static <T> List<T> selectRandomSubset (Collection<T> col, int count)
    {
        int csize = col.size();
        if (csize < count) {
            String errmsg = "Cannot select " + count + " elements " +
                "from a collection of only " + csize + " elements.";
            throw new IllegalArgumentException(errmsg);
        }

        ArrayList<T> subset = new ArrayList<T>(count);
        Iterator<T> iter = col.iterator();
        int s = 0;

        for (int k = 0; iter.hasNext(); k++) {
            T elem = iter.next();

            // the probability that an element is select for inclusion in our
            // random subset is proportional to the number of elements
            // remaining to be checked for inclusion divided by the number of
            // elements remaining to be included
            float limit = ((float)(count - s)) / ((float)(csize - k));

            // include the record if our random value is below the limit
            if (Math.random() < limit) {
                subset.add(elem);

                // stop looking if we've reached our target size
                if (++s == count) {
                    break;
                }
            }
        }

        return subset;
    }

    /**
     * If a collection contains only <code>Integer</code> objects, it can be
     * passed to this function and converted into an int array.
     *
     * @param col the collection to be converted.
     *
     * @return an int array containing the contents of the collection (in the
     * order returned by the collection's iterator). The size of the array will
     * be equal to the size of the collection.
     */
    public static int[] toIntArray (Collection<Integer> col)
    {
        Iterator<Integer> iter = col.iterator();
        int[] array = new int[col.size()];
        for (int i = 0; iter.hasNext(); i++) {
            array[i] = iter.next().intValue();
        }
        return array;
    }
}
