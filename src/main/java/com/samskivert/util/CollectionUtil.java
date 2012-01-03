//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.samskivert.annotation.ReplacedBy;

/**
 * A collection of collection-related utility functions.
 */
public class CollectionUtil
{
    /**
     * Adds all items returned by the enumeration to the supplied collection
     * and returns the supplied collection.
     */
    @ReplacedBy("com.google.common.collect.Iterators#addAll(Collection, com.google.common.collect.Iterators#forEnumeration(Enumeration))")
    public static <T, C extends Collection<T>> C addAll (C col, Enumeration<? extends T> enm)
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
    @ReplacedBy("com.google.common.collect.Iterators#addAll()")
    public static <T, C extends Collection<T>> C addAll (C col, Iterator<? extends T> iter)
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
    @ReplacedBy("java.util.Collections#addAll()")
    public static <T, E extends T, C extends Collection<T>> C addAll (C col, E[] values)
    {
        if (values != null) {
            for (E value : values) {
                col.add(value);
            }
        }
        return col;
    }

    /**
     * Folds all the specified values into the supplied collection and returns it.
     */
    public static <T, C extends Collection<T>> C addAll (
        C col, Iterable<? extends Collection<? extends T>> values)
    {
        for (Collection<? extends T> val : values) {
            col.addAll(val);
        }
        return col;
    }

    /**
     * Folds all the specified values into the supplied map and returns it.
     */
    public static <K, V, M extends Map<K, V>> M putAll (
        M map, Iterable<? extends Map<? extends K, ? extends V>> values)
    {
        for (Map<? extends K, ? extends V> val : values) {
            map.putAll(val);
        }
        return map;
    }

    /**
     * Modify the specified Collection so that only the first <code>limit</code> elements
     * remain, as determined by iteration order. If the Collection is smaller than limit,
     * it is unmodified.
     */
    public static void limit (Collection<?> col, int limit)
    {
        int size = col.size();
        if (size > limit) {
            if (col instanceof List<?>) {
                ((List<?>) col).subList(limit, size).clear();

            } else {
                Iterator<?> itr = col.iterator();
                for (int ii = 0; ii < limit; ii++) {
                    itr.next();
                }
                while (itr.hasNext()) {
                    itr.next();
                    itr.remove();
                }
            }
        }
    }

    /**
     * Return a List containing all the elements of the specified Iterable that compare as being
     * equal to the maximum element.
     *
     * @throws NoSuchElementException if the Iterable is empty.
     */
    public static <T extends Comparable<? super T>> List<T> maxList (Iterable<T> iterable)
    {
        return maxList(iterable, new Comparator<T>() {
            public int compare (T o1, T o2) {
                return o1.compareTo(o2);
            }
        });
    }

    /**
     * Return a List containing all the elements of the specified Iterable that compare as being
     * equal to the maximum element.
     *
     * @throws NoSuchElementException if the Iterable is empty.
     */
    public static <T> List<T> maxList (Iterable<T> iterable, Comparator<? super T> comp)
    {
        Iterator<T> itr = iterable.iterator();
        T max = itr.next();
        List<T> maxes = new ArrayList<T>();
        maxes.add(max);
        if (itr.hasNext()) {
            do {
                T elem = itr.next();
                int cmp = comp.compare(max, elem);
                if (cmp <= 0) {
                    if (cmp < 0) {
                        max = elem;
                        maxes.clear();
                    }
                    maxes.add(elem);
                }
            } while (itr.hasNext());

        } else if (0 != comp.compare(max, max)) {
            // The main point of this test is to compare the sole element to something,
            // in case it turns out to be incompatible with the Comparator for some reason.
            // In that case, we don't even get to this IAE, we've probably already bombed out
            // with an NPE or CCE. For example, the Comparable version could be called with
            // a sole element of null. (Collections.max() gets this wrong.)
            throw new IllegalArgumentException();
        }
        return maxes;
    }

    /**
     * Return a List containing all the elements of the specified Iterable that compare as being
     * equal to the minimum element.
     *
     * @throws NoSuchElementException if the Iterable is empty.
     */
    public static <T extends Comparable<? super T>> List<T> minList (Iterable<T> iterable)
    {
        return maxList(iterable, java.util.Collections.reverseOrder());
    }

    /**
     * Return a List containing all the elements of the specified Iterable that compare as being
     * equal to the minimum element.
     *
     * @throws NoSuchElementException if the Iterable is empty.
     */
    public static <T> List<T> minList (Iterable<T> iterable, Comparator<? super T> comp)
    {
        return maxList(iterable, java.util.Collections.reverseOrder(comp));
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
     * Returns an Array, of the type specified by the runtime-type token <code>type</code>,
     * containing the elements of the collection.
     */
    @ReplacedBy("com.google.common.collect.Iterables#toArray()")
    public static <T> T[] toArray (Collection<? extends T> col, Class<T> type)
    {
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(type, col.size());
        return col.toArray(array);
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
    @ReplacedBy("com.google.common.primitives.Ints#toArray()")
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
