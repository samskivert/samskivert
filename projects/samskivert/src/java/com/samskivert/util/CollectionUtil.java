//
// $Id: CollectionUtil.java,v 1.1 2001/07/21 22:05:04 mdb Exp $

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
}
