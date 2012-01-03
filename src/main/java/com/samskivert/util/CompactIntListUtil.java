//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * This class manages compact arrays of ints. It is similar to {@link
 * IntListUtil} except that the int arrays never contain empty slots and
 * expansion is done one element at a time. In spite of its reduced
 * computational efficiency, compact int lists are at times required.
 */
public class CompactIntListUtil
{
    /**
     * Adds the specified value to the list iff it is not already in the
     * list.
     *
     * @param list the list to which to add the value. Can be null.
     * @param value the value to add.
     *
     * @return a reference to the list with value added (might not be the
     * list you passed in due to expansion, or allocation).
     */
    public static int[] add (int[] list, int value)
    {
        // make sure we've got a list to work with
        if (list == null) {
            return new int[] { value };
        }

        // check to see if the element is in the list
        int llength = list.length;
        for (int i = 0; i < llength; i++) {
            if (list[i] == value) {
                return list;
            }
        }

        // expand the list and append our element
        int[] nlist = new int[llength+1];
        System.arraycopy(list, 0, nlist, 0, llength);
        nlist[llength] = value;

        return nlist;
    }

    /**
     * Looks for an element that is equal to the supplied value.
     *
     * @return true if a matching value was found, false otherwise.
     */
    public static boolean contains (int[] list, int value)
    {
        int llength = list.length; // no optimizing bastards
        for (int i = 0; i < llength; i++) {
            if (list[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks for an element that is equal to the supplied value and
     * returns its index in the array.
     *
     * @return the index of the first matching value if one was found,
     * -1 otherwise.
     */
    public static int indexOf (int[] list, int value)
    {
        int llength = list.length; // no optimizing bastards
        for (int i = 0; i < llength; i++) {
            if (list[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the first value that is equal to the supplied value. A new
     * array will be created containing all other elements, except the
     * located element, in the order they existed in the original list.
     *
     * @return the new array minus the found value, or the original array.
     */
    public static int[] remove (int[] list, int value)
    {
        // nothing to remove from an empty list
        if (list == null) {
            return null;
        }

        // search for the index of the element to be removed
        int llength = list.length; // no optimizing bastards
        for (int i = 0; i < llength; i++) {
            if (list[i] == value) {
                return removeAt(list, i);
            }
        }

        // if we didn't find it, we've nothing to do
        return list;
    }

    /**
     * Removes the value at the specified index. A new array will be
     * created containing all other elements, except the specified
     * element, in the order they existed in the original list.
     *
     * @return the new array minus the specified element.
     */
    public static int[] removeAt (int[] list, int index)
    {
        // this will NPE if the bastards passed a null list, which is how
        // we'll let them know not to do that
        int nlength = list.length-1;

        // create a new array minus the removed element
        int[] nlist = new int[nlength];
        System.arraycopy(list, 0, nlist, 0, index);
        System.arraycopy(list, index+1, nlist, index, nlength-index);

        return nlist;
    }
}
