//
// $Id: FormUtil.java,v 1.1 2001/02/15 01:44:34 mdb Exp $

package com.samskivert.webmacro;

import org.webmacro.servlet.WebContext;

/**
 * The form util class provides handy functions for doing form-related
 * stuff.
 */
public class FormUtil
{
    /**
     * Fetches the supplied parameter from the request and converts it to
     * an integer. If the parameter does not exist or is not a well-formed
     * integer, a data validation exception is thrown with the supplied
     * message.
     */
    public static int requireIntParameter (WebContext context, String name,
					   String invalidDataMessage)
	throws DataValidationException
    {
	String value = context.getForm(name);
	try {
	    return Integer.parseInt(value);
	} catch (NumberFormatException nfe) {
	    throw new DataValidationException(invalidDataMessage);
	}
    }
}
