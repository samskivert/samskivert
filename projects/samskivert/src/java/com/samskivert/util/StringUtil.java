//
// $Id: StringUtil.java,v 1.12 2001/10/18 21:50:18 mdb Exp $
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

package com.samskivert.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

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

    /**
     * Returns a new string based on <code>source</code> with all
     * instances of <code>before</code> replaced with <code>after</code>.
     */
    public static String replace (String source, String before, String after)
    {
	int pos = source.indexOf(before);
	if (pos == -1) {
	    return source;
	}

	StringBuffer sb = new StringBuffer(source.length() + 32);

	int blength = before.length();
	int start = 0;
	while (pos != -1) {
	    sb.append(source.substring(start, pos));
	    sb.append(after);
	    start = pos + blength;
	    pos = source.indexOf(before, start);
	}
	sb.append(source.substring(start));

	return sb.toString();
    }

    /**
     * Converts the supplied object to a string. Normally this is
     * accomplished via the object's built in <code>toString()</code>
     * method, but in the case of arrays, <code>toString()</code> is
     * called on each element and the contents are listed like so:
     *
     * <pre>
     * (value, value, value)
     * </pre>
     *
     * Arrays of ints, longs, floats and doubles are also handled for
     * convenience.
     *
     * <p> Additionally, <code>Enumeration</code> or <code>Iterator</code>
     * objects can be passed and they will be enumerated and output in a
     * similar manner to arrays. Bear in mind that this uses up the
     * enumeration or iterator in question.
     *
     * <p> Also note that passing null will result in the string "null"
     * being returned.
     */
    public static String toString (Object val)
    {
	StringBuffer buf = new StringBuffer();
        toString(buf, val);
	return buf.toString();
    }

    /**
     * Converts the supplied value to a string and appends it to the
     * supplied string buffer. See the single argument version for more
     * information.
     *
     * @param buf the string buffer to which we will append the string.
     * @param val the value from which to generate the string.
     */
    public static void toString (StringBuffer buf, Object val)
    {
	if (val instanceof int[]) {
	    buf.append("(");
	    int[] v = (int[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(v[i]);
	    }
	    buf.append(")");

	} else if (val instanceof long[]) {
	    buf.append("(");
	    long[] v = (long[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(v[i]);
	    }
	    buf.append(")");

	} else if (val instanceof float[]) {
	    buf.append("(");
	    float[] v = (float[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(v[i]);
	    }
	    buf.append(")");

	} else if (val instanceof double[]) {
	    buf.append("(");
	    double[] v = (double[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(v[i]);
	    }
	    buf.append(")");

	} else if (val instanceof Object[]) {
	    buf.append("(");
	    Object[] v = (Object[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(toString(v[i]));
	    }
	    buf.append(")");

	} else if (val instanceof Enumeration) {
	    buf.append("(");
            Enumeration enum = (Enumeration)val;
	    for (int i = 0; enum.hasMoreElements(); i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(toString(enum.nextElement()));
	    }
	    buf.append(")");

	} else if (val instanceof Iterator) {
	    buf.append("(");
            Iterator iter = (Iterator)val;
	    for (int i = 0; iter.hasNext(); i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(toString(iter.next()));
	    }
	    buf.append(")");

	} else {
	    buf.append(val);
	}
    }

    /**
     * Generates a string from the supplied bytes that is the HEX encoded
     * representation of those bytes.
     *
     * @param bytes the bytes for which we want a string representation.
     * @param count the number of bytes to stop at (which will be coerced
     * into being <= the length of the array).
     */
    public static String hexlate (byte[] bytes, int count)
    {
        char[] chars = new char[bytes.length*2];
        count = Math.min(count, bytes.length);

        for (int i = 0; i < count; i++) {
            int val = (int)bytes[i];
            if (val < 0) {
		val += 256;
	    }
            chars[2*i] = XLATE.charAt(val/16);
            chars[2*i+1] = XLATE.charAt(val%16);
        }

        return new String(chars);
    }

    /**
     * Generates a string from the supplied bytes that is the HEX encoded
     * representation of those bytes.
     */
    public static String hexlate (byte[] bytes)
    {
        return hexlate(bytes, bytes.length);
    }

    /**
     * Parses an array of integers from it's string representation. The
     * array should be represented as a bare list of numbers separated by
     * commas, for example:
     *
     * <pre>
     * 25, 17, 21, 99
     * </pre>
     *
     * Any inability to parse the int array will result in the function
     * returning null.
     */
    public static int[] parseIntArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        int[] vals = new int[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                String token = tok.nextToken().trim();
                vals[i] = Integer.parseInt(token);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return vals;
    }

    /**
     * Parses an array of strings from a single string. The array should
     * be represented as a bare list of strings separated by commas, for
     * example:
     *
     * <pre>
     * mary, had, a, little, lamb, and, an, escaped, comma,,
     * </pre>
     *
     * If a comma is desired in one of the strings, it should be escaped
     * by putting two commas in a row. Any inability to parse the string
     * array will result in the function returning null.
     */
    public static String[] parseStringArray (String source)
    {
        // sort out escaped commas
        source = replace(source, ",,", "%COMMA%");
        // now tokenize the string
        StringTokenizer tok = new StringTokenizer(source, ",");
        String[] vals = new String[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            // trim the whitespace from the token
            String token = tok.nextToken().trim();
            // undo the comma escaping
            token = replace(token, "%COMMA%", ",");
            vals[i] = token;
        }
        return vals;
    }

    /**
     * Splits the supplied string into components based on the specified
     * separator character.
     */
    public static String[] split (String source, String sep)
    {
        StringTokenizer tok = new StringTokenizer(source, sep);
        String[] tokens = new String[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            tokens[i] = tok.nextToken();
        }
        return tokens;
    }

    private final static String XLATE = "0123456789abcdef";
}
