//
// $Id: QuickSort.java,v 1.1 2002/02/19 03:37:31 mdb Exp $
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

import java.util.Comparator;

/**
 * A class to sort arrays of objects (quickly even)
 */
public class QuickSort
{
    /**
     * Sorts the supplied array of objects from least to greatest, using
     * the supplied comparator.
     */
    public static void sort (Object[] a, Comparator comp)
    {
	csort(a, 0, a.length - 1, comp);
    }

    /**
     * Sorts the supplied array of comparable objects from least to
     * greatest.
     */
    public static void sort (Comparable[] a)
    {
	sort(a, 0, a.length - 1);
    }

    /**
     * Sorts the supplied array of objects from greatest to least, using
     * the supplied comparator.
     */
    public static void rsort (Object[] a, Comparator comp)
    {
	crsort(a, 0, a.length - 1, comp);
    }

    /**
     * Sorts the supplied array of comparable objects from greatest to
     * least.
     */
    public static void rsort (Comparable[] a)
    {
        rsort(a, 0, a.length - 1);
    }

    /**
     * Sorts the specified subset of the supplied array from least to
     * greatest, using the supplied comparator.
     *
     * @param a the array of objects to be sorted.
     * @param lo0 the index of the lowest element to be included in the
     * sort.
     * @param hi0 the index of the highest element to be included in the
     * sort.
     * @param comp the comparator to use to establish ordering between
     * elements.
     */
    public static void csort (Object[] a, int lo0, int hi0, Comparator comp)
    {
        // bail out if we're already done
	if (hi0 <= lo0) {
            return;
        }

        Object t;

        // if this is a two element file, do a simple sort on it
        if (hi0 - lo0 == 1) {
            // if they're not already sorted, swap them
            if (comp.compare(a[hi0], a[lo0]) < 0) {
                t = a[lo0]; a[lo0] = a[hi0]; a[hi0] = t;
            }
            return;
        }

        // the middle element in the array is our partitioning element
        Object mid = a[(lo0 + hi0)/2];

        // set up our partitioning boundaries
        int lo = lo0-1, hi = hi0+1;

        // loop through the array until indices cross
        for (;;) {
            // find the first element that is greater than or equal to
            // the partition element starting from the left Index.
            while (comp.compare(a[++lo], mid) < 0);

            // find an element that is smaller than or equal to
            // the partition element starting from the right Index.
            while (comp.compare(mid, a[--hi]) < 0);

            // swap the two elements or bail out of the loop
            if (hi > lo) {
                t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            } else {
                break;
            }
        }

        // if the right index has not reached the left side of array
        // must now sort the left partition
        if (lo0 < lo-1) {
            csort(a, lo0, lo-1, comp);
        }

        // if the left index has not reached the right side of array
        // must now sort the right partition
        if (hi+1 < hi0) {
            csort(a, hi+1, hi0, comp);
        }
    }

    /**
     * Sorts the specified subset of the supplied array from greatest to
     * least, using the supplied comparator.
     *
     * @param a the array of objects to be sorted.
     * @param lo0 the index of the lowest element to be included in the
     * sort.
     * @param hi0 the index of the highest element to be included in the
     * sort.
     * @param comp the comparator to use to establish ordering between
     * elements.
     */
    public static void crsort (Object[] a, int lo0, int hi0, Comparator comp)
    {
        // bail out if we're already done
	if (hi0 <= lo0) {
            return;
        }

        Object t;

        // if this is a two element file, do a simple sort on it
        if (hi0 - lo0 == 1) {
            // if they're not already sorted, swap them
            if (comp.compare(a[lo0], a[hi0]) < 0) {
                t = a[lo0]; a[lo0] = a[hi0]; a[hi0] = t;
            }
            return;
        }

        // the middle element in the array is our partitioning element
        Object mid = a[(lo0 + hi0)/2];

        // set up our partitioning boundaries
        int lo = lo0-1, hi = hi0+1;

        // loop through the array until indices cross
        for (;;) {
            // find the first element that is greater than or equal to
            // the partition element starting from the left Index.
            while (comp.compare(mid, a[++lo]) < 0);

            // find an element that is smaller than or equal to
            // the partition element starting from the right Index.
            while (comp.compare(a[--hi], mid) < 0);

            // swap the two elements or bail out of the loop
            if (hi > lo) {
                t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            } else {
                break;
            }
        }

        // if the right index has not reached the left side of array
        // must now sort the left partition
        if (lo0 < lo-1) {
            crsort(a, lo0, lo-1, comp);
        }

        // if the left index has not reached the right side of array
        // must now sort the right partition
        if (hi+1 < hi0) {
            crsort(a, hi+1, hi0, comp);
        }
    }

    /**
     * Sorts the specified subset of the supplied array of comparables
     * from least to greatest, using the supplied comparator.
     *
     * @param a the array of objects to be sorted.
     * @param lo0 the index of the lowest element to be included in the
     * sort.
     * @param hi0 the index of the highest element to be included in the
     * sort.
     */
    public static void sort (Comparable[] a, int lo0, int hi0)
    {
        // bail out if we're already done
	if (hi0 <= lo0) {
            return;
        }

        Comparable t;

        // if this is a two element file, do a simple sort on it
        if (hi0 - lo0 == 1) {
            // if they're not already sorted, swap them
            if (a[lo0].compareTo(a[hi0]) > 0) {
                t = a[lo0]; a[lo0] = a[hi0]; a[hi0] = t;
            }
            return;
        }

        // the middle element in the array is our partitioning element
        Comparable mid = a[(lo0 + hi0)/2];

        // set up our partitioning boundaries
        int lo = lo0-1, hi = hi0+1;

        // loop through the array until indices cross
        for (;;) {
            // find the first element that is greater than or equal to
            // the partition element starting from the left Index.
            while (mid.compareTo(a[++lo]) > 0);

            // find an element that is smaller than or equal to
            // the partition element starting from the right Index.
            while (mid.compareTo(a[--hi]) < 0);

            // swap the two elements or bail out of the loop
            if (hi > lo) {
                t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            } else {
                break;
            }
        }

        // if the right index has not reached the left side of array
        // must now sort the left partition
        if (lo0 < lo-1) {
            sort(a, lo0, lo-1);
        }

        // if the left index has not reached the right side of array
        // must now sort the right partition
        if (hi+1 < hi0) {
            sort(a, hi+1, hi0);
        }
    }

    /**
     * Sorts the specified subset of the supplied array of comparables
     * from greatest to least, using the supplied comparator.
     *
     * @param a the array of objects to be sorted.
     * @param lo0 the index of the lowest element to be included in the
     * sort.
     * @param hi0 the index of the highest element to be included in the
     * sort.
     */
    public static void rsort (Comparable[] a, int lo0, int hi0)
    {
        // bail out if we're already done
	if (hi0 <= lo0) {
            return;
        }

        Comparable t;

        // if this is a two element file, do a simple sort on it
        if (hi0 - lo0 == 1) {
            // if they're not already sorted, swap them
            if (a[lo0].compareTo(a[hi0]) < 0) {
                t = a[lo0]; a[lo0] = a[hi0]; a[hi0] = t;
            }
            return;
        }

        // the middle element in the array is our partitioning element
        Comparable mid = a[(lo0 + hi0)/2];

        // set up our partitioning boundaries
        int lo = lo0-1, hi = hi0+1;

        // loop through the array until indices cross
        for (;;) {
            // find the first element that is greater than or equal to
            // the partition element starting from the left Index.
            while (mid.compareTo(a[++lo]) < 0);

            // find an element that is smaller than or equal to
            // the partition element starting from the right Index.
            while (mid.compareTo(a[--hi]) > 0);

            // swap the two elements or bail out of the loop
            if (hi > lo) {
                t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            } else {
                break;
            }
        }

        // if the right index has not reached the left side of array
        // must now sort the left partition
        if (lo0 < lo-1) {
            rsort(a, lo0, lo-1);
        }

        // if the left index has not reached the right side of array
        // must now sort the right partition
        if (hi+1 < hi0) {
            rsort(a, hi+1, hi0);
        }
    }
}
