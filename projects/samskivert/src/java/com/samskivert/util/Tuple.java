//
// $Id: Tuple.java,v 1.1 2001/05/29 03:29:54 mdb Exp $

package com.samskivert.util;

/**
 * A tuple is a simple object that holds a reference to two other objects.
 */
public class Tuple
{
    /** The left object. */
    public Object left;

    /** The right object. */
    public Object right;

    /** Construct a tuple with the specified two objects. */
    public Tuple (Object left, Object right)
    {
        this.left = left;
        this.right = right;
    }

    /** Construct a blank tuple. */
    public Tuple ()
    {
    }

    public String toString ()
    {
        return "[left=" + left + ", right=" + right + "]";
    }
}
