//
// $Id: DataValidationException.java,v 1.1 2001/02/15 01:44:34 mdb Exp $

package com.samskivert.webmacro;

/**
 * A data validation exception is thrown when a value supplied in a form
 * element is not valid. The servlet framework will substitute the message
 * assosciated with this exception into the context with the key
 * <code>"invalid_data"</code>. Templates should be structured so that
 * this is reported to the user along with the filled out form field for
 * their editing and resubmitting pleasures.
 */
public class DataValidationException extends FriendlyException
{
    public DataValidationException (String message)
    {
	super(message);
    }
}
