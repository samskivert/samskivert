//
// $Id: Enumerator.java,v 1.1 2001/07/04 18:22:26 mdb Exp $

package com.samskivert.viztool.enum;

/**
 * An enumerator is used to iterate over a set of classes.
 */
public interface Enumerator
{
    /**
     * Returns true if this enumerator has more classes yet to enumerate,
     * false if all classes have been reported.
     */
    public boolean hasMoreClasses ();

    /**
     * Returns the class name of the next class in the enumeration.
     */
    public String nextClass ();
}
