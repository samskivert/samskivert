//
// $Id: ObjectUtil.java,v 1.1 2003/08/18 21:53:59 ray Exp $

package com.samskivert.util;

/**
 * Utility methods that don't fit anywhere else.
 */
public class ObjectUtil
{
    /**
     * Test two objects for equality safely.
     */
    public static boolean equals (Object o1, Object o2)
    {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }
}
