//
// $Id: StringUtil.java,v 1.4 2001/03/02 01:49:48 mdb Exp $

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
     * convenience. Also note that passing null will result in the string
     * "null" being returned.
     */
    public static String toString (Object val)
    {
	StringBuffer buf = new StringBuffer();

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
		buf.append(v[i]);
	    }
	    buf.append(")");

	} else {
	    buf.append(val);
	}

	return buf.toString();
    }

    /**
     * Generates a string from the supplied bytes that is the HEX encoded
     * representation of those bytes.
     */
    public static String hexlate (byte[] bytes)
    {
        char[] chars = new char[bytes.length*2];

        for (int i = 0; i < chars.length; i++) {
            int val = (int)chars[i];
            if (val < 0) {
		val += 256;
	    }
            chars[2*i] = XLATE.charAt(val/16);
            chars[2*i+1] = XLATE.charAt(val%16);
        }

        return new String(chars);
    }

    private final static String XLATE = "0123456789abcdef";
}
