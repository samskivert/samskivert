//
// $Id: ParameterUtil.java,v 1.3 2002/02/16 01:14:56 shaper Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.servlet.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.Log;
import com.samskivert.util.StringUtil;

/**
 * Utility functions for fetching and manipulating request parameters
 * (form fields).
 */
public class ParameterUtil
{
    /**
     * Fetches the supplied parameter from the request. If the parameter
     * does not exist, either null or the empty string will be returned
     * depending on the value of the <code>returnNull</code> parameter.
     */
    public static String getParameter (
        HttpServletRequest req, String name, boolean returnNull)
    {
	String value = req.getParameter(name);
        if (returnNull || !StringUtil.blank(value)) {
            return value;
        } else {
            return "";
        }
    }

    /**
     * Fetches the supplied parameter from the request and converts it to
     * a float. If the parameter does not exist or is not a well-formed
     * float, a data validation exception is thrown with the supplied
     * message.
     */
    public static float requireFloatParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
	return parseFloatParameter(
            getParameter(req, name, false), invalidDataMessage);
        
    }

    protected static float parseFloatParameter (
        String value, String invalidDataMessage)
        throws DataValidationException
    {
        try {
	    return Float.parseFloat(value);
	} catch (NumberFormatException nfe) {
	    throw new DataValidationException(invalidDataMessage);
	}
    }

    /**
     * Fetches the supplied parameter from the request and converts it to
     * an integer. If the parameter does not exist or is not a well-formed
     * integer, a data validation exception is thrown with the supplied
     * message.
     */
    public static int requireIntParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
	return parseIntParameter(
            getParameter(req, name, false), invalidDataMessage);
        
    }

    /**
     * Fetches the supplied parameter from the request and converts it to
     * an integer. If the parameter does not exist, <code>defval</code> is
     * returned. If the parameter is not a well-formed integer, a data
     * validation exception is thrown with the supplied message.
     */
    public static int getIntParameter (
        HttpServletRequest req, String name, int defval,
        String invalidDataMessage)
	throws DataValidationException
    {
	String value = getParameter(req, name, false);
        if (StringUtil.blank(value)) {
            return defval;
        }
        return parseIntParameter(value, invalidDataMessage);
    }

    protected static int parseIntParameter (
        String value, String invalidDataMessage)
        throws DataValidationException
    {
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
    public static String requireParameter (
        HttpServletRequest req, String name, String missingDataMessage)
	throws DataValidationException
    {
	String value = req.getParameter(name);
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
    public static Date requireDateParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
        return parseDateParameter(getParameter(req, name, false),
                                  invalidDataMessage);
    }

    /**
     * Fetches the supplied parameter from the request and converts it to
     * a date. The value of the parameter should be a date formatted like
     * so: 2001-12-25. If the parameter does not exist, null is
     * returned. If the parameter is not a well-formed date, a data
     * validation exception is thrown with the supplied message.
     */
    public static Date getDateParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
	String value = getParameter(req, name, false);
        if (StringUtil.blank(value)) {
            return null;
        }
        return parseDateParameter(value, invalidDataMessage);
    }

    protected static
        Date parseDateParameter (String value, String invalidDataMessage)
	throws DataValidationException
    {
	try {
            return _dparser.parse(value);
	} catch (ParseException pe) {
	    throw new DataValidationException(invalidDataMessage);
	}
    }

    /**
     * Returns true if the specified parameter is set in the request.
     *
     * @return true if the specified parameter is set in the request
     * context, false otherwise.
     */
    public static boolean isSet (HttpServletRequest req, String name)
    {
	return !StringUtil.blank(req.getParameter(name));
    }

    /**
     * Returns true if the specified parameter is equal to the supplied
     * value. If the parameter is not set in the request, false is
     * returned.
     *
     * @param name The parameter whose value should be compared with the
     * supplied value.
     * @param value The value to which the parameter may be equal. This
     * should not be null.
     *
     * @return true if the specified parameter is equal to the supplied
     * parameter, false otherwise.
     */
    public static boolean parameterEquals (
        HttpServletRequest req, String name, String value)
    {
	return value.equals(getParameter(req, name, false));
    }

    /** We use this to parse dates in requireDateParameter(). */
    protected static SimpleDateFormat _dparser =
	new SimpleDateFormat("yyyy-MM-dd");
}
