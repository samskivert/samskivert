//
// $Id: FormatException.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends;

/**
 * Used to communicate file format exceptions.
 */
public class FormatException extends Exception
{
    public FormatException (String message)
    {
        super(message);
    }
}
