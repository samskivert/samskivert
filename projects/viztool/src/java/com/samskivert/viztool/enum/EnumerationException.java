//
// $Id: EnumerationException.java,v 1.1 2001/06/14 20:57:15 mdb Exp $

package com.samskivert.viztool.enum;

/**
 * An enumeration exception is thrown when some problem occurs while
 * attempting to enumerate over a classpath component. This may be when
 * initially attempting to read a zip or jar file, or during the process
 * of enumeration.
 */
public class EnumerationException extends Exception
{
    public EnumerationException (String message)
    {
        super(message);
    }
}
