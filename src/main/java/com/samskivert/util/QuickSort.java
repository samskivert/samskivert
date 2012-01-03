//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Collections;
import java.util.List;
import java.util.Comparator;

/**
 * A class to sort arrays of objects (quickly even).
 */
public class QuickSort
{
    /**
     * Sorts the supplied array of objects from least to greatest, using the supplied comparator.
     */
    public static <T> void sort (T[] a, Comparator<? super T> comp)
    {
        sort(a, 0, a.length - 1, comp);
    }

    /**
     * Sorts the supplied array of comparable objects from least to greatest.
     */
    public static <T extends Comparable<? super T>> void sort (T[] a)
    {
        sort(a, 0, a.length - 1);
    }

    /**
     * Sorts the supplied array of objects from greatest to least, using the supplied comparator.
     */
    public static <T> void rsort (T[] a, Comparator<? super T> comp)
    {
        rsort(a, 0, a.length - 1, comp);
    }

    /**
     * Sorts the supplied array of comparable objects from greatest to least.
     */
    public static <T extends Comparable<? super T>> void rsort (T[] a)
    {
        rsort(a, 0, a.length - 1);
    }

    /**
     * Sorts the specified subset of the supplied array from least to greatest, using the supplied
     * comparator.
     *
     * @param a the array of objects to be sorted.
     * @param lo0 the index of the lowest element to be included in the sort.
     * @param hi0 the index of the highest element to be included in the sort.
     * @param comp the comparator to use to establish ordering between elements.
     */
    public static <T> void sort (T[] a, int lo0, int hi0, Comparator<? super T> comp)
    {
        // bail out if we're already done
        if (hi0 <= lo0) {
            return;
        }

        T t;

        // if this is a two element file, do a simple sort on it
        if (hi0 - lo0 == 1) {
            // if they're not already sorted, swap them
            if (comp.compare(a[hi0], a[lo0]) < 0) {
                t = a[lo0]; a[lo0] = a[hi0]; a[hi0] = t;
            }
            return;
        }

        // the middle element in the array is our partitioning element
        T mid = a[(lo0 + hi0) >>> 1];

        // set up our partitioning boundaries
        int lo = lo0-1, hi = hi0+1;

        // loop through the array until indices cross
        for (;;) {
            // find the first element that is greater than or equal to the partition element
            // starting from the left index
            while (comp.compare(a[++lo], mid) < 0) { /* loop! */ }

            // find an element that is smaller than or equal to the partition element starting from
            // the right index
            while (comp.compare(mid, a[--hi]) < 0) { /* loop! */ }

            // swap the two elements or bail out of the loop
            if (hi > lo) {
                t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            } else {
                break;
            }
        }

        // if the right index has not reached the left side of array must now sort the left
        // partition
        if (lo0 < lo-1) {
            sort(a, lo0, lo-1, comp);
        }

        // if the left index has not reached the right side of array must now sort the right
        // partition
        if (hi+1 < hi0) {
            sort(a, hi+1, hi0, comp);
        }
    }

    /**
     * Sorts the specified subset of the supplied array from greatest to least, using the supplied
     * comparator.
     *
     * @param a the array of objects to be sorted.
     * @param lo0 the index of the lowest element to be included in the sort.
     * @param hi0 the index of the highest element to be included in the sort.
     * @param comp the comparator to use to establish ordering between elements.
     */
    public static <T> void rsort (T[] a, int lo0, int hi0, Comparator<? super T> comp)
    {
        // bail out if we're already done
        if (hi0 <= lo0) {
            return;
        }

        T t;

        // if this is a two element file, do a simple sort on it
        if (hi0 - lo0 == 1) {
            // if they're not already sorted, swap them
            if (comp.compare(a[lo0], a[hi0]) < 0) {
                t = a[lo0]; a[lo0] = a[hi0]; a[hi0] = t;
            }
            return;
        }

        // the middle element in the array is our partitioning element
        T mid = a[(lo0 + hi0) >>> 1];

        // set up our partitioning boundaries
        int lo = lo0-1, hi = hi0+1;

        // loop through the array until indices cross
        for (;;) {
            // find the first element that is greater than or equal to the partition element
            // starting from the left index
            while (comp.compare(mid, a[++lo]) < 0) { /* loop! */ }

            // find an element that is smaller than or equal to the partition element starting from
            // the right index
            while (comp.compare(a[--hi], mid) < 0) { /* loop! */ }

            // swap the two elements or bail out of the loop
            if (hi > lo) {
                t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            } else {
                break;
            }
        }

        // if the right index has not reached the left side of array must now sort the left
        // partition
        if (lo0 < lo-1) {
            rsort(a, lo0, lo-1, comp);
        }

        // if the left index has not reached the right side of array must now sort the right
        // partition
        if (hi+1 < hi0) {
            rsort(a, hi+1, hi0, comp);
        }
    }

    /**
     * Sorts the specified subset of the supplied array of comparables from least to greatest,
     * using the supplied comparator.
     *
     * @param a the array of objects to be sorted.
     * @param lo0 the index of the lowest element to be included in the sort.
     * @param hi0 the index of the highest element to be included in the sort.
     */
    public static <T extends Comparable<? super T>> void sort (T[] a, int lo0, int hi0)
    {
        // bail out if we're already done
        if (hi0 <= lo0) {
            return;
        }

        T t;

        // if this is a two element file, do a simple sort on it
        if (hi0 - lo0 == 1) {
            // if they're not already sorted, swap them
            if (a[lo0].compareTo(a[hi0]) > 0) {
                t = a[lo0]; a[lo0] = a[hi0]; a[hi0] = t;
            }
            return;
        }

        // the middle element in the array is our partitioning element
        T mid = a[(lo0 + hi0) >>> 1];

        // set up our partitioning boundaries
        int lo = lo0-1, hi = hi0+1;

        // loop through the array until indices cross
        for (;;) {
            // find the first element that is greater than or equal to the partition element
            // starting from the left Index.
            while (mid.compareTo(a[++lo]) > 0) { /* loop! */ }

            // find an element that is smaller than or equal to the partition element starting from
            // the right Index.
            while (mid.compareTo(a[--hi]) < 0) { /* loop! */ }

            // swap the two elements or bail out of the loop
            if (hi > lo) {
                t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            } else {
                break;
            }
        }

        // if the right index has not reached the left side of array must now sort the left
        // partition
        if (lo0 < lo-1) {
            sort(a, lo0, lo-1);
        }

        // if the left index has not reached the right side of array must now sort the right
        // partition
        if (hi+1 < hi0) {
            sort(a, hi+1, hi0);
        }
    }

    /**
     * Sorts the specified subset of the supplied array of comparables from greatest to least,
     * using the supplied comparator.
     *
     * @param a the array of objects to be sorted.
     * @param lo0 the index of the lowest element to be included in the sort.
     * @param hi0 the index of the highest element to be included in the sort.
     */
    public static <T extends Comparable<? super T>> void rsort (T[] a, int lo0, int hi0)
    {
        // bail out if we're already done
        if (hi0 <= lo0) {
            return;
        }

        T t;

        // if this is a two element file, do a simple sort on it
        if (hi0 - lo0 == 1) {
            // if they're not already sorted, swap them
            if (a[lo0].compareTo(a[hi0]) < 0) {
                t = a[lo0]; a[lo0] = a[hi0]; a[hi0] = t;
            }
            return;
        }

        // the middle element in the array is our partitioning element
        T mid = a[(lo0 + hi0) >>> 1];

        // set up our partitioning boundaries
        int lo = lo0-1, hi = hi0+1;

        // loop through the array until indices cross
        for (;;) {
            // find the first element that is greater than or equal to the partition element
            // starting from the left index
            while (mid.compareTo(a[++lo]) < 0) { /* loop! */ }

            // find an element that is smaller than or equal to the partition element starting from
            // the right index
            while (mid.compareTo(a[--hi]) > 0) { /* loop! */ }

            // swap the two elements or bail out of the loop
            if (hi > lo) {
                t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            } else {
                break;
            }
        }

        // if the right index has not reached the left side of array must now sort the left
        // partition
        if (lo0 < lo-1) {
            rsort(a, lo0, lo-1);
        }

        // if the left index has not reached the right side of array must now sort the right
        // partition
        if (hi+1 < hi0) {
            rsort(a, hi+1, hi0);
        }
    }

    /**
     * Sort the elements in the specified List according to their natural order.
     */
    public static <T extends Comparable<? super T>> void sort (List<T> a)
    {
        sort(a, new Comparator<T>() {
            public int compare (T o1, T o2) {
                if (o1 == o2) { // catches null == null
                    return 0;
                } else if (o1 == null) {
                    return 1;
                } else if (o2 == null) {
                    return -1;
                }
                return o1.compareTo(o2); // null-free
            }
        });
    }

    /**
     * Sort the elements in the specified List according to the ordering imposed by the specified
     * Comparator.
     */
    public static <T> void sort (List<T> a, Comparator<? super T> comp)
    {
        sort(a, 0, a.size() - 1, comp);
    }

    /**
     * Sort the elements in the specified List according to their reverse natural order.
     */
    public static <T extends Comparable<? super T>> void rsort (List<T> a)
    {
        sort(a, new Comparator<T>() {
            public int compare (T o1, T o2) {
                if (o1 == o2) { // catches null == null
                    return 0;
                } else if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                }
                return o2.compareTo(o1); // null-free
            }
        });
    }

    /**
     * Sort the elements in the specified List according to the reverse ordering imposed by the
     * specified Comparator.
     */
    public static <T> void rsort (List<T> a, Comparator<? super T> comp)
    {
        sort(a, Collections.reverseOrder(comp));
    }

    /**
     * Sort a subset of the elements in the specified List according to the ordering imposed by the
     * specified Comparator.
     */
    public static <T> void sort (List<T> a, int lo0, int hi0, Comparator<? super T> comp)
    {
        // bail out if we're already done
        if (hi0 <= lo0) {
            return;
        }

        T e1, e2;

        // if this is a two element file, do a simple sort on it
        if (hi0 - lo0 == 1) {
            // if they're not already sorted, swap them
            e1 = a.get(lo0);
            e2 = a.get(hi0);
            if (comp.compare(e2, e1) < 0) {
                a.set(hi0, e1);
                a.set(lo0, e2);
            }
            return;
        }

        // the middle element in the array is our partitioning element
        T mid = a.get((lo0 + hi0) >>> 1);

        // set up our partitioning boundaries
        int lo = lo0-1, hi = hi0+1;

        // loop through the array until indices cross
        for (;;) {
            // find the first element that is greater than or equal to the partition element
            // starting from the left index
            do {
                e1 = a.get(++lo);
            } while (comp.compare(e1, mid) < 0);

            // find an element that is smaller than or equal to the partition element starting from
            // the right index
            do {
                e2 = a.get(--hi);
            } while (comp.compare(mid, e2) < 0);

            // swap the two elements or bail out of the loop
            if (hi > lo) {
                a.set(lo, e2);
                a.set(hi, e1);
            } else {
                break;
            }
        }

        // if the right index has not reached the left side of array must now sort the left
        // partition
        if (lo0 < lo-1) {
            sort(a, lo0, lo-1, comp);
        }

        // if the left index has not reached the right side of array must now sort the right
        // partition
        if (hi+1 < hi0) {
            sort(a, hi+1, hi0, comp);
        }
    }
}
