//
// $Id: StringUtil.java,v 1.72 2004/03/06 20:05:41 ray Exp $
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

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.io.UnsupportedEncodingException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.net.URLEncoder;
import java.net.URLDecoder;

import java.text.NumberFormat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Truncate the specified String if it is longer than maxLength.
     */
    public static String truncate (String s, int maxLength)
    {
        return truncate(s, maxLength, "");
    }

    /**
     * Truncate the specified String if it is longer than maxLength.
     *
     * @param append a String to add to the truncated String only after
     * truncation.
     */
    public static String truncate (String s, int maxLength, String append)
    {
        if ((s == null) || (s.length() <= maxLength)) {
            return s;
        } else {
            return s.substring(0, maxLength) + append;
        }
    }

    /**
     * Returns a version of the supplied string with the first letter
     * capitalized.
     */
    public static String capitalize (String s)
    {
        if (blank(s)) {
            return s;
        }
        char c = s.charAt(0);
        if (Character.isUpperCase(c)) {
            return s;
        } else {
            return String.valueOf(Character.toUpperCase(c)) + s.substring(1);
        }
    }

    /**
     * Validates a character.
     */
    public static interface CharacterValidator
    {
        public boolean isValid (char c);
    }

    /**
     * Sanitize the specified String so that only valid characters are in it.
     */
    public static String sanitize (String source, CharacterValidator validator)
    {
        int nn = source.length();
        StringBuffer buf = new StringBuffer(nn);
        for (int ii=0; ii < nn; ii++) {
            char c = source.charAt(ii);
            if (validator.isValid(c)) {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * Sanitize the specified String such that each character must match
     * against the regex specified.
     */
    public static String sanitize (String source, String charRegex)
    {
        final StringBuffer buf = new StringBuffer(" ");
        final Matcher matcher = Pattern.compile(charRegex).matcher(buf);
        return sanitize(source, new CharacterValidator() {
            public boolean isValid (char c) {
                buf.setCharAt(0, c);
                return matcher.matches();
            }
        });
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
     * Pads the supplied string to the requested string width by appending
     * spaces to the end of the returned string. If the original string is
     * wider than the requested width, it is returned unmodified.
     */
    public static String pad (String value, int width)
    {
        // sanity check
        if (width <= 0) {
            String errmsg = "Pad width must be greater than zero.";
            throw new IllegalArgumentException(errmsg);

        } else if (value.length() >= width) {
            return value;

        } else {
            return value + spaces(width-value.length());
        }
    }

    /**
     * Pads the supplied string to the requested string width by prepending
     * spaces to the end of the returned string. If the original string is
     * wider than the requested width, it is returned unmodified.
     */
    public static String prepad (String value, int width)
    {
        // sanity check
        if (width <= 0) {
            String errmsg = "Pad width must be greater than zero.";
            throw new IllegalArgumentException(errmsg);

        } else if (value.length() >= width) {
            return value;

        } else {
            return spaces(width-value.length()) + value;
        }
    }

    /**
     * Returns a string containing the requested number of spaces.
     */
    public static String spaces (int count)
    {
        return fill(' ', count);
    }

    /**
     * Returns a string containing the specified character repeated the
     * specified number of times.
     */
    public static String fill (char c, int count)
    {
        StringBuffer buf = new StringBuffer();
        for (int ii = 0; ii < count; ii++) {
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Returns whether the supplied string represents an integer value by
     * attempting to parse it with {@link Integer#parseInt}.
     */
    public static boolean isInteger (String value)
    {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException nfe) {
            // fall through
        }
        return false;
    }

    /**
     * Formats a floating point value with useful default rules;
     * ie. always display a digit to the left of the decimal and display
     * only two digits to the right of the decimal (rounding as
     * necessary).
     */
    public static String format (float value)
    {
        return _ffmt.format(value);
    }

    /**
     * Formats a floating point value with useful default rules;
     * ie. always display a digit to the left of the decimal and display
     * only two digits to the right of the decimal (rounding as
     * necessary).
     */
    public static String format (double value)
    {
        return _ffmt.format(value);
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
     * Like the single argument {@link #toString(Object)} with the
     * additional function of specifying the characters that are used to
     * box in list and array types. For example, if "[" and "]" were
     * supplied, an int array might be formatted like so: <code>[1, 3,
     * 5]</code>.
     */
    public static String toString (
        Object val, String openBox, String closeBox)
    {
	StringBuffer buf = new StringBuffer();
        toString(buf, val, openBox, closeBox);
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
        toString(buf, val, "(", ")");
    }

    /**
     * Converts the supplied value to a string and appends it to the
     * supplied string buffer. The specified boxing characters are used to
     * enclose list and array types. For example, if "[" and "]" were
     * supplied, an int array might be formatted like so: <code>[1, 3,
     * 5]</code>.
     *
     * @param buf the string buffer to which we will append the string.
     * @param val the value from which to generate the string.
     * @param openBox the opening box character.
     * @param closeBox the closing box character.
     */
    public static void toString (StringBuffer buf, Object val,
                                 String openBox, String closeBox)
    {
        toString(buf, val, openBox, closeBox, ", ");
    }

    /**
     * Converts the supplied value to a string and appends it to the
     * supplied string buffer. The specified boxing characters are used to
     * enclose list and array types. For example, if "[" and "]" were
     * supplied, an int array might be formatted like so: <code>[1, 3,
     * 5]</code>.
     *
     * @param buf the string buffer to which we will append the string.
     * @param val the value from which to generate the string.
     * @param openBox the opening box character.
     * @param closeBox the closing box character.
     * @param sep the separator string.
     */
    public static void toString (StringBuffer buf, Object val,
                                 String openBox, String closeBox, String sep)
    {
	if (val instanceof byte[]) {
	    buf.append(openBox);
	    byte[] v = (byte[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(v[i]);
	    }
	    buf.append(closeBox);

        } else if (val instanceof short[]) {
	    buf.append(openBox);
	    short[] v = (short[])val;
	    for (short i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(v[i]);
	    }
	    buf.append(closeBox);

        } else if (val instanceof int[]) {
	    buf.append(openBox);
	    int[] v = (int[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(v[i]);
	    }
	    buf.append(closeBox);

	} else if (val instanceof long[]) {
	    buf.append(openBox);
	    long[] v = (long[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(v[i]);
	    }
	    buf.append(closeBox);

	} else if (val instanceof float[]) {
	    buf.append(openBox);
	    float[] v = (float[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(v[i]);
	    }
	    buf.append(closeBox);

	} else if (val instanceof double[]) {
	    buf.append(openBox);
	    double[] v = (double[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(v[i]);
	    }
	    buf.append(closeBox);

	} else if (val instanceof Object[]) {
	    buf.append(openBox);
	    Object[] v = (Object[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(toString(v[i]));
	    }
	    buf.append(closeBox);

	} else if (val instanceof boolean[]) {
	    buf.append(openBox);
	    boolean[] v = (boolean[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(v[i] ? "t" : "f");
	    }
	    buf.append(closeBox);

	} else if (val instanceof Collection) {
            toString(buf, ((Collection)val).iterator(), openBox, closeBox);

	} else if (val instanceof Enumeration) {
	    buf.append(openBox);
            Enumeration enum = (Enumeration)val;
	    for (int i = 0; enum.hasMoreElements(); i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(toString(enum.nextElement()));
	    }
	    buf.append(closeBox);

	} else if (val instanceof Iterator) {
	    buf.append(openBox);
            Iterator iter = (Iterator)val;
	    for (int i = 0; iter.hasNext(); i++) {
		if (i > 0) {
		    buf.append(sep);
		}
		buf.append(toString(iter.next()));
	    }
	    buf.append(closeBox);

	} else if (val instanceof Point2D) {
            Point2D p = (Point2D)val;
	    buf.append(openBox);
            coordsToString(buf, (int)p.getX(), (int)p.getY());
            buf.append(closeBox);

	} else if (val instanceof Dimension2D) {
            Dimension2D d = (Dimension2D)val;
	    buf.append(openBox);
            buf.append(d.getWidth()).append("x").append(d.getHeight());
            buf.append(closeBox);

	} else if (val instanceof Rectangle2D) {
            Rectangle2D r = (Rectangle2D)val;
            buf.append(openBox);
            buf.append(r.getWidth()).append("x").append(r.getHeight());
            coordsToString(buf, (int)r.getX(), (int)r.getY());
            buf.append(closeBox);

	} else {
	    buf.append(val);
	}
    }

    /**
     * Used to format objects in {@link
     * #listToString(Object,StringUtil.Formatter)}.
     */
    public static class Formatter
    {
        /**
         * Formats the supplied object into a string.
         */
        public String toString (Object object)
        {
            return object == null ? "null" : object.toString();
        }

        /**
         * Returns the string that will be prepended to a formatted list.
         */
        public String getOpenBox ()
        {
            return "(";
        }

        /**
         * Returns the string that will be appended to a formatted list.
         */
        public String getCloseBox ()
        {
            return ")";
        }
    }

    /**
     * Formats a collection of elements (either an array of objects, an
     * {@link Iterator}, an {@link Enumeration} or a {@link Collection})
     * using the supplied formatter on each element. Note that if you
     * simply wish to format a collection of elements by calling {@link
     * Object#toString} on each element, you can just pass the list to the
     * {@link #toString(Object)} method which will do just that.
     */
    public static String listToString (Object val, Formatter formatter)
    {
        StringBuffer buf = new StringBuffer();
        listToString(buf, val, formatter);
        return buf.toString();
    }

    /**
     * Formats the supplied collection into the supplied string buffer
     * using the supplied formatter. See {@link
     * #listToString(Object,StringUtil.Formatter)} for more details.
     */
    public static void listToString (
        StringBuffer buf, Object val, Formatter formatter)
    {
        // get an iterator if this is a collection
        if (val instanceof Collection) {
            val = ((Collection)val).iterator();
        }

        String openBox = formatter.getOpenBox();
        String closeBox = formatter.getCloseBox();

	if (val instanceof Object[]) {
	    buf.append(openBox);
	    Object[] v = (Object[])val;
	    for (int i = 0; i < v.length; i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(formatter.toString(v[i]));
	    }
	    buf.append(closeBox);

	} else if (val instanceof Collection) {
            listToString(buf, ((Collection)val).iterator(), formatter);

	} else if (val instanceof Enumeration) {
	    buf.append(openBox);
            Enumeration enum = (Enumeration)val;
	    for (int i = 0; enum.hasMoreElements(); i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(formatter.toString(enum.nextElement()));
	    }
	    buf.append(closeBox);

	} else if (val instanceof Iterator) {
	    buf.append(openBox);
            Iterator iter = (Iterator)val;
	    for (int i = 0; iter.hasNext(); i++) {
		if (i > 0) {
		    buf.append(", ");
		}
		buf.append(formatter.toString(iter.next()));
	    }
	    buf.append(closeBox);

        } else {
            // fall back on the general purpose
            toString(buf, val);
        }
    }

    /**
     * Generates a string representation of the supplied object by calling
     * {@link #toString} on the contents of its public fields and
     * prefixing that by the name of the fields. For example:
     *
     * <p><code>[itemId=25, itemName=Elvis, itemCoords=(14, 25)]</code>
     */
    public static String fieldsToString (Object object)
    {
        return fieldsToString(object, ", ");
    }

    /**
     * Like {@link #fieldsToString(Object)} except that the supplied
     * separator string will be used between fields.
     */
    public static String fieldsToString (Object object, String sep)
    {
        StringBuffer buf = new StringBuffer("[");
        fieldsToString(buf, object, sep);
        return buf.append("]").toString();
    }

    /**
     * Appends to the supplied string buffer a representation of the
     * supplied object by calling {@link #toString} on the contents of its
     * public fields and prefixing that by the name of the fields. For
     * example:
     *
     * <p><code>itemId=25, itemName=Elvis, itemCoords=(14, 25)</code>
     *
     * <p>Note: unlike the version of this method that returns a string,
     * enclosing brackets are not included in the output of this method.
     */
    public static void fieldsToString (StringBuffer buf, Object object)
    {
        fieldsToString(buf, object, ", ");
    }

    /**
     * Like {@link #fieldsToString(StringBuffer,Object)} except that the
     * supplied separator will be used between fields.
     */
    public static void fieldsToString (
        StringBuffer buf, Object object, String sep)
    {
        Class clazz = object.getClass();
        Field[] fields = clazz.getFields();
        int written = 0;

        // we only want non-static fields
        for (int i = 0; i < fields.length; i++) {
            int mods = fields[i].getModifiers();
            if ((mods & Modifier.PUBLIC) == 0 ||
                (mods & Modifier.STATIC) != 0) {
                continue;
            }

            if (written > 0) {
                buf.append(sep);
            }

            // look for a toString() method for this field
            buf.append(fields[i].getName()).append("=");
            try {
                try {
                    Method meth = clazz.getMethod(
                        fields[i].getName() + "ToString", new Class[0]);
                    buf.append(meth.invoke(object, null));
                } catch (NoSuchMethodException nsme) {
                    toString(buf, fields[i].get(object));
                }
            } catch (Exception e) {
                buf.append("<error: " + e + ">");
            }
            written++;
        }
    }

    /**
     * Formats a pair of coordinates such that positive values are
     * rendered with a plus prefix and negative values with a minus
     * prefix.  Examples would look like: <code>+3+4</code>
     * <code>-5+7</code>, etc.
     */
    public static String coordsToString (int x, int y)
    {
        StringBuffer buf = new StringBuffer();
        coordsToString(buf, x, y);
        return buf.toString();
    }

    /**
     * Formats a pair of coordinates such that positive values are
     * rendered with a plus prefix and negative values with a minus
     * prefix.  Examples would look like: <code>+3+4</code>
     * <code>-5+7</code>, etc.
     */
    public static void coordsToString (StringBuffer buf, int x, int y)
    {
        if (x >= 0) {
            buf.append("+");
        }
        buf.append(x);
        if (y >= 0) {
            buf.append("+");
        }
        buf.append(y);
    }

    /**
     * Attempts to generate a string representation of the object using
     * <code>object.toString()</code>, but catches any exceptions that are
     * thrown and reports them in the returned string instead. Useful for
     * situations where you can't trust the rat bastards that implemented
     * the object you're toString()ing.
     */
    public static String safeToString (Object object)
    {
        try {
            return object.toString();
        } catch (Exception e) {
            return "<toString() failure: " + e + ">";
        }
    }

    /**
     * URL encodes the specified string using the UTF-8 character encoding.
     */
    public static String encode (String s)
    {
        try {
            return (s != null) ? URLEncoder.encode(s, "UTF-8") : null;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("UTF-8 is unknown in this Java.");
        }
    }

    /**
     * URL decodes the specified string using the UTF-8 character encoding.
     */
    public static String decode (String s)
    {
        try {
            return (s != null) ? URLDecoder.decode(s, "UTF-8") : null;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("UTF-8 is unknown in this Java.");
        }
    }

    /**
     * Generates a string from the supplied bytes that is the HEX encoded
     * representation of those bytes.  Returns the empty string for a
     * <code>null</code> or empty byte array.
     *
     * @param bytes the bytes for which we want a string representation.
     * @param count the number of bytes to stop at (which will be coerced
     * into being <= the length of the array).
     */
    public static String hexlate (byte[] bytes, int count)
    {
        if (bytes == null) {
            return "";
        }

        count = Math.min(count, bytes.length);
        char[] chars = new char[count*2];

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
        return (bytes == null) ? "" : hexlate(bytes, bytes.length);
    }

    /**
     * Turn a hexlated String back into a byte array.
     */
    public static byte[] unhexlate (String hex)
    {
        if (hex == null || (hex.length() % 2 != 0)) {
            return null;
        }

        // if for some reason we are given a hex string that wasn't made
        // by hexlate, convert to lowercase so things work.
        hex = hex.toLowerCase();
        byte[] data = new byte[hex.length()/2];
        for (int ii = 0; ii < hex.length(); ii+=2) {
            int value = (byte)(XLATE.indexOf(hex.charAt(ii)) << 4);
            value  += XLATE.indexOf(hex.charAt(ii+1));

            // values over 127 are wrapped around, restoring negative bytes
            data[ii/2] = (byte)value;
        }

        return data;
    }

    /**
     * Returns a hex string representing the MD5 encoded source.
     *
     * @exception RuntimeException thrown if the MD5 codec was not
     * available in this JVM.
     */
    public static String md5hex (String source)
    {
	try {
	    MessageDigest digest = MessageDigest.getInstance("MD5");
	    return hexlate(digest.digest(source.getBytes()));
	} catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("MD5 codec not available");
	}
    }

    /**
     * Parses an array of signed byte-sized integers from their string
     * representation. The array should be represented as a bare list of
     * numbers separated by commas, for example:
     *
     * <pre>
     * 25, 17, 21, 99
     * </pre>
     *
     * Any inability to parse the short array will result in the function
     * returning null.
     */
    public static byte[] parseByteArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        byte[] vals = new byte[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                String token = tok.nextToken().trim();
                vals[i] = Byte.parseByte(token);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return vals;
    }

    /**
     * Parses an array of short integers from their string representation.
     * The array should be represented as a bare list of numbers separated
     * by commas, for example:
     *
     * <pre>
     * 25, 17, 21, 99
     * </pre>
     *
     * Any inability to parse the short array will result in the function
     * returning null.
     */
    public static short[] parseShortArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        short[] vals = new short[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                String token = tok.nextToken().trim();
                vals[i] = Short.parseShort(token);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return vals;
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
     * Parses an array of floats from it's string representation. The
     * array should be represented as a bare list of numbers separated by
     * commas, for example:
     *
     * <pre>
     * 25.0, .5, 1, 0.99
     * </pre>
     *
     * Any inability to parse the array will result in the function
     * returning null.
     */
    public static float[] parseFloatArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        float[] vals = new float[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                String token = tok.nextToken().trim();
                vals[i] = Float.parseFloat(token);
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
        return parseStringArray(source, false);
    }

    /**
     * Like {@link #parseStringArray(String)} but can be instructed to
     * invoke {@link String#intern} on the strings being parsed into the
     * array.
     */
    public static String[] parseStringArray (String source, boolean intern)
    {
        int tcount = 0, tpos = -1, tstart = 0;

        // empty strings result in zero length arrays
        if (source.length() == 0) {
            return new String[0];
        }

        // sort out escaped commas
        source = replace(source, ",,", "%COMMA%");

        // count up the number of tokens
        while ((tpos = source.indexOf(",", tpos+1)) != -1) {
            tcount++;
        }

        String[] tokens = new String[tcount+1];
        tpos = -1; tcount = 0;

        // do the split
        while ((tpos = source.indexOf(",", tpos+1)) != -1) {
            tokens[tcount] = source.substring(tstart, tpos);
            tokens[tcount] = replace(tokens[tcount].trim(), "%COMMA%", ",");
            if (intern) {
                tokens[tcount] = tokens[tcount].intern();
            }
            tstart = tpos+1;
            tcount++;
        }

        // grab the last token
        tokens[tcount] = source.substring(tstart);
        tokens[tcount] = replace(tokens[tcount].trim(), "%COMMA%", ",");

        return tokens;
    }

    /**
     * Joins an array of strings (or objects which will be converted to
     * strings) into a single string separated by commas.
     */
    public static String join (Object[] values)
    {
        return join(values, false);
    }

    /**
     * Joins an array of strings into a single string, separated by
     * commas, and optionally escaping commas that occur in the individual
     * string values such that a subsequent call to {@link
     * #parseStringArray} would recreate the string array properly. Any
     * elements in the values array that are null will be treated as an
     * empty string.
     */
    public static String join (Object[] values, boolean escape)
    {
        return join(values, ", ", escape);
    }

    /**
     * Joins the supplied array of strings into a single string separated
     * by the supplied separator.
     */
    public static String join (Object[] values, String separator)
    {
        return join(values, separator, false);
    }

    /**
     * Helper function for the various <code>join</code> methods.
     */
    protected static String join (
        Object[] values, String separator, boolean escape)
    {
        StringBuffer buf = new StringBuffer();
        int vlength = values.length;
        for (int i = 0; i < vlength; i++) {
            if (i > 0) {
                buf.append(separator);
            }
            String value = (values[i] == null) ? "" : values[i].toString();
            buf.append((escape) ? replace(value, ",", ",,") : value);
        }
        return buf.toString();
    }

    /**
     * Joins an array of strings into a single string, separated by
     * commas, and escaping commas that occur in the individual string
     * values such that a subsequent call to {@link #parseStringArray}
     * would recreate the string array properly. Any elements in the
     * values array that are null will be treated as an empty string.
     */
    public static String joinEscaped (String[] values)
    {
        return join(values, true);
    }

    /**
     * Splits the supplied string into components based on the specified
     * separator string.
     */
    public static String[] split (String source, String sep)
    {
        int tcount = 0, tpos = -1, tstart = 0;

        // count up the number of tokens
        while ((tpos = source.indexOf(sep, tpos+1)) != -1) {
            tcount++;
        }

        String[] tokens = new String[tcount+1];
        tpos = -1; tcount = 0;

        // do the split
        while ((tpos = source.indexOf(sep, tpos+1)) != -1) {
            tokens[tcount] = source.substring(tstart, tpos);
            tstart = tpos+1;
            tcount++;
        }

        // grab the last token
        tokens[tcount] = source.substring(tstart);
        tokens[tcount] = replace(tokens[tcount].trim(), "%COMMA%", ",");

        return tokens;
    }

    /**
     * Returns an array containing the values in the supplied array
     * converted into a table of values wrapped at the specified column
     * count and fit into the specified field width. For example, a call
     * like <code>toWrappedString(values, 5, 3)</code> might result in
     * output like so:
     *
     * <pre>
     *  12  1  9 10  3
     *   1  5  7  9 11
     *  39 15 12 80 16
     * </pre>
     */
    public static String toMatrixString (
        int[] values, int colCount, int fieldWidth)
    {
        StringBuffer buf = new StringBuffer();
        StringBuffer valbuf = new StringBuffer();

        for (int i = 0; i < values.length; i++) {
            // format the integer value
            valbuf.setLength(0);
            valbuf.append(values[i]);

            // pad with the necessary spaces
            int spaces = fieldWidth - valbuf.length();
            for (int s = 0; s < spaces; s++) {
                buf.append(" ");
            }

            // append the value itself
            buf.append(valbuf);

            // if we're at the end of a row but not the end of the whole
            // integer list, append a newline
            if (i % colCount == (colCount-1) &&
                i != values.length-1) {
                buf.append("\n");
            }
        }

        return buf.toString();
    }

    /**
     * Used to convert a time interval to a more easily human readable
     * string of the form: <code>1d 15h 4m 15s 987m</code>.
     */
    public static String intervalToString (long millis)
    {
        StringBuffer buf = new StringBuffer();
        boolean started = false;

        long days = millis / (24 * 60 * 60 * 1000);
        if (days != 0) {
            buf.append(days).append("d ");
            started = true;
        }

        long hours = (millis / (60 * 60 * 1000)) % 24;
        if (started || hours != 0) {
            buf.append(hours).append("h ");
        }

        long minutes = (millis / (60 * 1000)) % 60;
        if (started || minutes != 0) {
            buf.append(minutes).append("m ");
        }

        long seconds = (millis / (1000)) % 60;
        if (started || seconds != 0) {
            buf.append(seconds).append("s ");
        }

        buf.append(millis % 1000).append("m");

        return buf.toString();
    }

    /**
     * Returns the class name of the supplied object, truncated to one
     * package prior to the actual class name. For example,
     * <code>com.samskivert.util.StringUtil</code> would be reported as
     * <code>util.StringUtil</code>. If a null object is passed in,
     * <code>null</code> is returned.
     */
    public static String shortClassName (Object object)
    {
        return (object == null) ? "null" : shortClassName(object.getClass());
    }

    /**
     * Returns the supplied class's name, truncated to one package prior
     * to the actual class name. For example,
     * <code>com.samskivert.util.StringUtil</code> would be reported as
     * <code>util.StringUtil</code>.
     */
    public static String shortClassName (Class clazz)
    {
        String name = clazz.getName();
        int didx = name.lastIndexOf(".");
        if (didx == -1) {
            return name;
        }
        didx = name.lastIndexOf(".", didx-1);
        if (didx == -1) {
            return name;
        }
        return name.substring(didx+1);
    }

    private final static String XLATE = "0123456789abcdef";

    /** Used to easily format floats with sensible defaults. */
    protected static final NumberFormat _ffmt = NumberFormat.getInstance();
    static {
        _ffmt.setMinimumIntegerDigits(1);
        _ffmt.setMinimumFractionDigits(1);
        _ffmt.setMaximumFractionDigits(2);
    }
}
