//
// $Id: StringUtil.java,v 1.2 2000/10/31 00:51:13 mdb Exp $

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
}
