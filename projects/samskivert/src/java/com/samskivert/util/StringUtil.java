//
// $Id: StringUtil.java,v 1.1 2000/10/31 00:04:15 mdb Exp $

package com.samskivert.util;

/**
 * String related utility functions.
 */
public class StringUtil
{
    /**
     * @return true if the string is null or empty, false otherwise.
     */
    public static boolean blank (String value)
    {
	return (value == null || value.trim().length() == 0);
    }
}
