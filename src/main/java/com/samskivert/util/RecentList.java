//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Arrays;

/**
 * An array list that maintains a list of the N most recent entries added
 * to it.
 */
public class RecentList
{
    /**
     * Constructs a recent list that keeps the specified number of most
     * recently added objects.
     */
    public RecentList (int keepCount)
    {
        _list = new Object[keepCount];
    }

    /**
     * Returns the number of elements in this list.
     */
    public int size ()
    {
        return Math.min(_lastPos, _list.length);
    }

    /**
     * Clears the list.
     */
    public void clear ()
    {
        Arrays.fill(_list, null);
        _lastPos = 0;
    }

    /**
     * Adds the specified value to the list.
     */
    public void add (Object value)
    {
        _list[_lastPos++ % _list.length] = value;
        // keep last pos cycling from keepCount to 2*keepCount-1
        if (_lastPos == 2*_list.length) {
            _lastPos = _list.length;
        }
    }

    /**
     * Returns true if the supplied value is equal (using {@link
     * Object#equals}) to any value in the list.
     */
    public boolean contains (Object value)
    {
        for (int ii = 0, nn = size(); ii < nn; ii++) {
            if (ObjectUtil.equals(value, _list[ii])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the Nth most recently added value from the list.
     * <code>null</code> is returned if the list does not contain at least
     * <code>index+1</code> entries.
     */
    public Object get (int index)
    {
        return _list[(_lastPos-index-1+_list.length)%_list.length];
    }

    public static void main (String[] args)
    {
        RecentList list = new RecentList(5);
        for (int ii = 0; ii < 10; ii++) {
            list.add(ii);
        }
        System.out.println("Contains 3 " + list.contains(3));
        System.out.println("Contains 7 " + list.contains(7));
        for (int ii = 0; ii < list.size(); ii++) {
            System.out.println(ii + " => " + list.get(ii));
        }
    }

    protected Object[] _list;
    protected int _lastPos;
}
