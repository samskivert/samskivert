//
// $Id: FormUtil.java,v 1.2 2001/03/01 21:06:22 mdb Exp $

package com.samskivert.webmacro;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.samskivert.util.StringUtil;
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

    /**
     * Fetches the supplied parameter from the request. If the parameter
     * does not exist, a data validation exception is thrown with the
     * supplied message.
     */
    public static String requireParameter (WebContext context, String name,
					   String missingDataMessage)
	throws DataValidationException
    {
	String value = context.getForm(name);
	if (StringUtil.blank(value)) {
	    throw new DataValidationException(missingDataMessage);
	}
	return value;
    }

    /**
     * Fetches the supplied parameter from the request and converts it to
     * a date. The value of the parameter should be a date formatted like
     * so: 2001-12-25. If the parameter does not exist or is not a
     * well-formed date, a data validation exception is thrown with the
     * supplied message.
     */
    public static Date requireDateParameter (WebContext context, String name,
					     String invalidDataMessage)
	throws DataValidationException
    {
	String value = context.getForm(name);
	Date date = null;

	try {
	    if (value != null) {
		date = _dparser.parse(value);
	    }
	} catch (ParseException pe) {
	    // fall through with date == null
	    Log.info("Date parsing failed: " + pe);
	}

	// freak out if we failed to parse the date for some reason
	if (date == null) {
	    throw new DataValidationException(invalidDataMessage);
	}

	return date;
    }

    /**
     * Returns true if the specified parameter is set in the request
     * context.
     *
     * @return true if the specified parameter is set in the request
     * context, false otherwise.
     */
    public static boolean isSet (WebContext context, String name)
    {
	return !StringUtil.blank(context.getForm(name));
    }

    /**
     * Returns true if the specified parameter is equal to the supplied
     * value. If the parameter is not set in the request context, false is
     * returned.
     *
     * @param context The request context.
     * @param name The parameter whose value should be compared with the
     * supplied value.
     * @param value The value to which the parameter may be equal. This
     * should not be null.
     *
     * @return true if the specified parameter is equal to the supplied
     * parameter, false otherwise.
     */
    public static boolean equals (WebContext context, String name,
				  String value)
    {
	return value.equals(context.getForm(name));
    }

    /** We use this to parse dates in requireDateParameter(). */
    protected static SimpleDateFormat _dparser =
	new SimpleDateFormat("yyyy-MM-dd");
}
