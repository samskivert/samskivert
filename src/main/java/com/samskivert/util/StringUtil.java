//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.samskivert.annotation.ReplacedBy;

/**
 * String related utility functions.
 */
public class StringUtil
{
    /**
     * Used to format objects in {@link #listToString(Object,StringUtil.Formatter)}.
     */
    public static class Formatter
    {
        /** Formats the supplied object into a string. */
        public String toString (Object object) {
            return object == null ? "null" : object.toString();
        }

        /** Returns the string that will be prepended to a formatted list. */
        public String getOpenBox () {
            return "(";
        }

        /** Returns the string that will be appended to a formatted list. */
        public String getCloseBox () {
            return ")";
        }
    }

    /**
     * @return true if the string is null or empty, false otherwise.
     *
     * @deprecated use isBlank instead.
     */
    @Deprecated
    public static boolean blank (String value)
    {
        return isBlank(value);
    }

    /**
     * @return true if the string is null or consists only of whitespace, false otherwise.
     */
    public static boolean isBlank (String value)
    {
        for (int ii = 0, ll = (value == null) ? 0 : value.length(); ii < ll; ii++) {
            if (!Character.isWhitespace(value.charAt(ii))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calls {@link String#trim} on non-null values, returns null for null values.
     */
    public static String trim (String value)
    {
        return (value == null) ? null : value.trim();
    }

    /**
     * @return the supplied string if it is non-null, "" if it is null.
     */
    public static String deNull (String value)
    {
        return (value == null) ? "" : value;
    }

    /**
     * Truncate the specified String if it is longer than maxLength.
     */
    public static String truncate (String s, int maxLength)
    {
        return truncate(s, maxLength, "");
    }

    /**
     * Returns the string if it is non-blank (see {@link #isBlank}), the default value otherwise.
     */
    public static String getOr (String value, String defval)
    {
        return isBlank(value) ? defval : value;
    }

    /**
     * Truncate the specified String if it is longer than maxLength.  The string will be truncated
     * at a position such that it is maxLength chars long after the addition of the 'append'
     * String.
     *
     * @param append a String to add to the truncated String only after truncation.
     */
    public static String truncate (String s, int maxLength, String append)
    {
        if ((s == null) || (s.length() <= maxLength)) {
            return s;
        } else {
            return s.substring(0, maxLength - append.length()) + append;
        }
    }

    /**
     * Returns a version of the supplied string with the first letter capitalized.
     */
    public static String capitalize (String s)
    {
        if (isBlank(s)) {
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
     * Returns a US locale lower case string.  Useful when manipulating filenames and resource
     * keys which would not have locale specific characters.
     */
    public static String toUSLowerCase (String s)
    {
        return isBlank(s) ? s : s.toLowerCase(Locale.US);
    }

    /**
     * Returns a US locale upper case string.  Useful when manipulating filenames and resource
     * keys which would not have locale specific characters.
     */
    public static String toUSUpperCase (String s)
    {
        return isBlank(s) ? s : s.toUpperCase(Locale.US);
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
        if (source == null) {
            return null;
        }
        int nn = source.length();
        StringBuilder buf = new StringBuilder(nn);
        for (int ii=0; ii < nn; ii++) {
            char c = source.charAt(ii);
            if (validator.isValid(c)) {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * Sanitize the specified String such that each character must match against the regex
     * specified.
     */
    public static String sanitize (String source, String charRegex)
    {
        final StringBuilder buf = new StringBuilder(" ");
        final Matcher matcher = Pattern.compile(charRegex).matcher(buf);
        return sanitize(source, new CharacterValidator() {
            public boolean isValid (char c) {
                buf.setCharAt(0, c);
                return matcher.matches();
            }
        });
    }

    /**
     * Returns a new string based on <code>source</code> with all instances of <code>before</code>
     * replaced with <code>after</code>.
     *
     * @deprecated java.lang.String.replace() was added in 1.5
     */
    @Deprecated @ReplacedBy(value="java.lang.String.replace()", reason="since 1.5")
    public static String replace (String source, String before, String after)
    {
        int pos = source.indexOf(before);
        if (pos == -1) {
            return source;
        }

        StringBuilder sb = new StringBuilder(source.length() + 32);

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
     * Pads the supplied string to the requested string width by appending spaces to the end of the
     * returned string. If the original string is wider than the requested width, it is returned
     * unmodified.
     */
    public static String pad (String value, int width)
    {
        return pad(value, width, ' ');
    }

    /**
     * Pads the supplied string to the requested string width by appending the specified character
     * to the end of the returned string.
     * If the original string is wider than the requested width, it is returned unmodified.
     */
    public static String pad (String value, int width, char c)
    {
        // sanity check
        if (width <= 0) {
            throw new IllegalArgumentException("Pad width must be greater than zero.");
        }
        int l = value.length();
        return (l >= width) ? value
                            : value + fill(c, width - l);
    }

    /**
     * Pads the supplied string to the requested string width by prepending spaces to the beginning
     * of the string. If the original string is wider than the requested width, it is
     * returned unmodified.
     */
    public static String prepad (String value, int width)
    {
        return prepad(value, width, ' ');
    }

    /**
     * Pads the supplied string to the requested string width by prepending the specified character
     * to the beginning of the string.
     * If the original string is wider than the requested width, it is returned unmodified.
     */
    public static String prepad (String value, int width, char c)
    {
        // sanity check
        if (width <= 0) {
            throw new IllegalArgumentException("Pad width must be greater than zero.");
        }
        int l = value.length();
        return (l >= width) ? value
                            : fill(c, width - l) + value;
    }

    /**
     * Returns a string containing the requested number of spaces.
     */
    public static String spaces (int count)
    {
        return fill(' ', count);
    }

    /**
     * Returns a string containing the specified character repeated the specified number of times.
     */
    public static String fill (char c, int count)
    {
        char[] sameChars = new char[count];
        Arrays.fill(sameChars, c);
        return new String(sameChars);
    }

    /**
     * Returns whether the supplied string represents an integer value by attempting to parse it
     * with {@link Integer#parseInt}.
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
     * Format the specified int as a String color value, like "000000". You might want
     * to add a prefix like "#" or "0x", depending on your usage.
     */
    public static String toColorString (int c)
    {
        return prepad(Integer.toHexString(c), 6, '0');
    }

    /**
     * Formats a floating point value with useful default rules; ie. always display a digit to the
     * left of the decimal and display only two digits to the right of the decimal (rounding as
     * necessary).
     */
    public static String format (float value)
    {
        return _ffmt.format(value);
    }

    /**
     * Formats a floating point value with useful default rules; ie. always display a digit to the
     * left of the decimal and display only two digits to the right of the decimal (rounding as
     * necessary).
     */
    public static String format (double value)
    {
        return _ffmt.format(value);
    }

    /**
     * Converts the supplied object to a string. Normally this is accomplished via the object's
     * built in <code>toString()</code> method, but in the case of arrays, <code>toString()</code>
     * is called on each element and the contents are listed like so:
     * <pre>(value, value, value)</pre>
     * Arrays of primitive types are also handled for convenience.
     *
     * <p> Also note that passing null will result in the string "null" being returned.
     */
    public static String toString (Object val)
    {
        StringBuilder buf = new StringBuilder();
        toString(buf, val);
        return buf.toString();
    }

    /**
     * Converts the supplied value to a string and appends it to the supplied string buffer. See
     * {@link #toString()} for more information.
     *
     * @param buf the string buffer to which we will append the string.
     * @param val the value from which to generate the string.
     */
    public static void toString (StringBuilder buf, Object val)
    {
        // these boxes+sep will only be used for arrays
        toString(buf, val, "(", ")", ", ", false);
    }

    /**
     * Like the single argument {@link #toString(Object)} with the additional function of
     * specifying the characters that are used to box in collection and array types. For example,
     * if "[" and "]" were supplied, an int array might be formatted like so: <code>[1, 3,
     * 5]</code>.
     *
     * <p> Note: in this method (unlike {@link #toString()}), <code>Enumeration</code> or
     * <code>Iterator</code> objects can be passed and they will be enumerated and output in a
     * similar manner to arrays. Bear in mind that this uses up the enumeration or iterator in
     * question.
     */
    public static String toString (Object val, String openBox, String closeBox)
    {
        StringBuilder buf = new StringBuilder();
        toString(buf, val, openBox, closeBox);
        return buf.toString();
    }

    /**
     * Converts the supplied value to a string and appends it to the supplied string buffer. The
     * specified boxing characters are used to enclose list and array types. For example, if "["
     * and "]" were supplied, an int array might be formatted like so: <code>[1, 3, 5]</code>.
     *
     * <p> Note: in this method (unlike {@link #toString()}), <code>Enumeration</code> or
     * <code>Iterator</code> objects can be passed and they will be enumerated and output in a
     * similar manner to arrays. Bear in mind that this uses up the enumeration or iterator in
     * question.
     *
     * @param buf the string buffer to which we will append the string.
     * @param val the value from which to generate the string.
     * @param openBox the opening box character.
     * @param closeBox the closing box character.
     */
    public static void toString (StringBuilder buf, Object val, String openBox, String closeBox)
    {
        toString(buf, val, openBox, closeBox, ", ");
    }

    /**
     * Converts the supplied value to a string and appends it to the supplied string buffer. The
     * specified boxing characters are used to enclose list and array types. For example, if "["
     * and "]" were supplied, an int array might be formatted like so: <code>[1, 3, 5]</code>.
     *
     * <p> Note: in this method (unlike {@link #toString()}), <code>Enumeration</code> or
     * <code>Iterator</code> objects can be passed and they will be enumerated and output in a
     * similar manner to arrays. Bear in mind that this uses up the enumeration or iterator in
     * question.
     *
     * @param buf the string buffer to which we will append the string.
     * @param val the value from which to generate the string.
     * @param openBox the opening box character.
     * @param closeBox the closing box character.
     * @param sep the separator string.
     */
    public static void toString (StringBuilder buf, Object val, String openBox, String closeBox,
                                 String sep)
    {
        toString(buf, val, openBox, closeBox, sep, true);
    }

    /**
     * Formats a collection of elements (either an array of objects, an {@link Iterator}, an {@link
     * Iterable} or an {@link Enumeration}) using the supplied formatter on each element. Note
     * that if you simply wish to format a collection of elements by calling {@link
     * Object#toString} on each element, you can just pass the list to the {@link
     * #toString(Object)} method which will do just that.
     */
    public static String listToString (Object val, Formatter formatter)
    {
        StringBuilder buf = new StringBuilder();
        listToString(buf, val, formatter);
        return buf.toString();
    }

    /**
     * Formats the supplied collection into the supplied string buffer using the supplied
     * formatter. See {@link #listToString(Object,StringUtil.Formatter)} for more details.
     */
    public static void listToString (StringBuilder buf, Object val, Formatter formatter)
    {
        // get an iterator if this is a collection
        if (val instanceof Iterable<?>) {
            val = ((Iterable<?>)val).iterator();
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

        } else if (val instanceof Iterator<?>) {
            buf.append(openBox);
            Iterator<?> iter = (Iterator<?>)val;
            for (int i = 0; iter.hasNext(); i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(formatter.toString(iter.next()));
            }
            buf.append(closeBox);

        } else if (val instanceof Enumeration<?>) {
            buf.append(openBox);
            Enumeration<?> enm = (Enumeration<?>)val;
            for (int i = 0; enm.hasMoreElements(); i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(formatter.toString(enm.nextElement()));
            }
            buf.append(closeBox);

        } else {
            // fall back on the general purpose
            toString(buf, val);
        }
    }

    /**
     * Generates a string representation of the supplied object by calling {@link #toString} on the
     * contents of its public fields and prefixing that by the name of the fields. For example:
     *
     * <p><code>[itemId=25, itemName=Elvis, itemCoords=(14, 25)]</code>
     */
    public static String fieldsToString (Object object)
    {
        return fieldsToString(object, ", ");
    }

    /**
     * Like {@link #fieldsToString(Object)} except that the supplied separator string will be used
     * between fields.
     */
    public static String fieldsToString (Object object, String sep)
    {
        StringBuilder buf = new StringBuilder("[");
        fieldsToString(buf, object, sep);
        return buf.append("]").toString();
    }

    /**
     * Appends to the supplied string buffer a representation of the supplied object by calling
     * {@link #toString} on the contents of its public fields and prefixing that by the name of the
     * fields. For example:
     *
     * <p><code>itemId=25, itemName=Elvis, itemCoords=(14, 25)</code>
     *
     * <p>Note: unlike the version of this method that returns a string, enclosing brackets are not
     * included in the output of this method.
     */
    public static void fieldsToString (StringBuilder buf, Object object)
    {
        fieldsToString(buf, object, ", ");
    }

    /**
     * Like {@link #fieldsToString(StringBuilder,Object)} except that the supplied separator will
     * be used between fields.
     */
    public static void fieldsToString (
            StringBuilder buf, Object object, String sep)
    {
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getFields();
        int written = 0;

        // we only want non-static fields
        for (Field field : fields) {
            int mods = field.getModifiers();
            if ((mods & Modifier.PUBLIC) == 0 || (mods & Modifier.STATIC) != 0) {
                continue;
            }

            if (written > 0) {
                buf.append(sep);
            }

            // look for a toString() method for this field
            buf.append(field.getName()).append("=");
            try {
                try {
                    Method meth = clazz.getMethod(field.getName() + "ToString", new Class<?>[0]);
                    buf.append(meth.invoke(object, (Object[]) null));
                } catch (NoSuchMethodException nsme) {
                    toString(buf, field.get(object));
                }
            } catch (Exception e) {
                buf.append("<error: " + e + ">");
            }
            written++;
        }
    }

    /**
     * Formats a pair of coordinates such that positive values are rendered with a plus prefix and
     * negative values with a minus prefix.  Examples would look like: <code>+3+4</code>
     * <code>-5+7</code>, etc.
     */
    public static String coordsToString (int x, int y)
    {
        StringBuilder buf = new StringBuilder();
        coordsToString(buf, x, y);
        return buf.toString();
    }

    /**
     * Formats a pair of coordinates such that positive values are rendered with a plus prefix and
     * negative values with a minus prefix.  Examples would look like: <code>+3+4</code>
     * <code>-5+7</code>, etc.
     */
    public static void coordsToString (StringBuilder buf, int x, int y)
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
     * Attempts to generate a string representation of the object using {@link Object#toString},
     * but catches any exceptions that are thrown and reports them in the returned string
     * instead. Useful for situations where you can't trust the rat bastards that implemented the
     * object you're toString()ing.
     */
    public static String safeToString (Object object)
    {
        try {
            return toString(object);
        } catch (Throwable t) {
            // We catch any throwable, even Errors. Someone is just trying to debug something,
            // probably inside another catch block.
            return "<toString() failure: " + t + ">";
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
     * Generates a string from the supplied bytes that is the HEX encoded representation of those
     * bytes.  Returns the empty string for a <code>null</code> or empty byte array.
     *
     * @param bytes the bytes for which we want a string representation.
     * @param count the number of bytes to stop at (which will be coerced into being {@code <=} the
     * length of the array).
     */
    public static String hexlate (byte[] bytes, int count)
    {
        if (bytes == null) {
            return "";
        }

        count = Math.min(count, bytes.length);
        char[] chars = new char[count*2];

        for (int i = 0; i < count; i++) {
            int val = bytes[i];
            if (val < 0) {
                val += 256;
            }
            chars[2*i] = XLATE.charAt(val/16);
            chars[2*i+1] = XLATE.charAt(val%16);
        }

        return new String(chars);
    }

    /**
     * Generates a string from the supplied bytes that is the HEX encoded representation of those
     * bytes.
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

        // if for some reason we are given a hex string that wasn't made by hexlate, convert to
        // lowercase so things work.
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
     * Encodes the supplied source text into an MD5 hash.
     */
    public static byte[] md5 (String source)
    {
        return digest("MD5", source);
    }

    /**
     * Returns a hex string representing the MD5 encoded source.
     *
     * @exception RuntimeException thrown if the MD5 codec was not available in this JVM.
     */
    public static String md5hex (String source)
    {
        return hexlate(md5(source));
    }

    /**
     * Returns a hex string representing the SHA-1 encoded source.
     *
     * @exception RuntimeException thrown if the SHA-1 codec was not available in this JVM.
     */
    public static String sha1hex (String source)
    {
        return hexlate(digest("SHA-1", source));
    }

    /**
     * Parses an array of signed byte-sized integers from their string representation. The array
     * should be represented as a bare list of numbers separated by commas, for example:
     *
     * <pre>25, 17, 21, 99</pre>
     *
     * Any inability to parse the short array will result in the function returning null.
     */
    public static byte[] parseByteArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        byte[] vals = new byte[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                vals[i] = Byte.parseByte(tok.nextToken().trim());
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return vals;
    }

    /**
     * Parses an array of short integers from their string representation.  The array should be
     * represented as a bare list of numbers separated by commas, for example:
     *
     * <pre>25, 17, 21, 99</pre>
     *
     * Any inability to parse the short array will result in the function returning null.
     */
    public static short[] parseShortArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        short[] vals = new short[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                vals[i] = Short.parseShort(tok.nextToken().trim());
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return vals;
    }

    /**
     * Parses an array of integers from it's string representation. The array should be represented
     * as a bare list of numbers separated by commas, for example:
     *
     * <pre>25, 17, 21, 99</pre>
     *
     * Any inability to parse the int array will result in the function returning null.
     */
    public static int[] parseIntArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        int[] vals = new int[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                vals[i] = Integer.parseInt(tok.nextToken().trim());
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return vals;
    }

    /**
     * Parses an array of longs from it's string representation. The array should be represented as
     * a bare list of numbers separated by commas, for example:
     *
     * <pre>25, 17125141422, 21, 99</pre>
     *
     * Any inability to parse the long array will result in the function returning null.
     */
    public static long[] parseLongArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        long[] vals = new long[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                vals[i] = Long.parseLong(tok.nextToken().trim());
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return vals;
    }

    /**
     * Parses an array of floats from it's string representation. The array should be represented
     * as a bare list of numbers separated by commas, for example:
     *
     * <pre>25.0, .5, 1, 0.99</pre>
     *
     * Any inability to parse the array will result in the function returning null.
     */
    public static float[] parseFloatArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        float[] vals = new float[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                vals[i] = Float.parseFloat(tok.nextToken().trim());
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return vals;
    }

    /**
     * Parses an array of doubles from its string representation. The array should be represented
     * as a bare list of numbers separated by commas, for example:
     *
     * <pre>25.0, .5, 1, 0.99</pre>
     *
     * Any inability to parse the array will result in the function returning null.
     */
    public static double[] parseDoubleArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        double[] vals = new double[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                // trim the whitespace from the token
                vals[i] = Double.parseDouble(tok.nextToken().trim());
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return vals;
    }

    /**
     * Parses an array of booleans from its string representation. The array should be represented
     * as a bare list of numbers separated by commas, for example:
     *
     * <pre>false, false, true, false</pre>
     */
    public static boolean[] parseBooleanArray (String source)
    {
        StringTokenizer tok = new StringTokenizer(source, ",");
        boolean[] vals = new boolean[tok.countTokens()];
        for (int i = 0; tok.hasMoreTokens(); i++) {
            // accept a lone 't' for true for compatibility with toString(boolean[])
            String token = tok.nextToken().trim();
            vals[i] = Boolean.parseBoolean(token) || token.equalsIgnoreCase("t");
        }
        return vals;
    }

    /**
     * Parses an array of strings from a single string. The array should be represented as a bare
     * list of strings separated by commas, for example:
     *
     * <pre>mary, had, a, little, lamb, and, an, escaped, comma,,</pre>
     *
     * If a comma is desired in one of the strings, it should be escaped by putting two commas in a
     * row. Any inability to parse the string array will result in the function returning null.
     */
    public static String[] parseStringArray (String source)
    {
        return parseStringArray(source, false);
    }

    /**
     * Like {@link #parseStringArray(String)} but can be instructed to invoke {@link String#intern}
     * on the strings being parsed into the array.
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
     * Joins an array of strings (or objects which will be converted to strings) into a single
     * string separated by commas.
     */
    public static String join (Object[] values)
    {
        return join(values, false);
    }

    /**
     * Joins an array of strings into a single string, separated by commas, and optionally escaping
     * commas that occur in the individual string values such that a subsequent call to {@link
     * #parseStringArray} would recreate the string array properly. Any elements in the values
     * array that are null will be treated as an empty string.
     */
    public static String join (Object[] values, boolean escape)
    {
        return join(values, ", ", escape);
    }

    /**
     * Joins the supplied array of strings into a single string separated by the supplied
     * separator.
     */
    public static String join (Object[] values, String separator)
    {
        return join(values, separator, false);
    }

    /**
     * Joins an array of strings into a single string, separated by commas, and escaping commas
     * that occur in the individual string values such that a subsequent call to {@link
     * #parseStringArray} would recreate the string array properly. Any elements in the values
     * array that are null will be treated as an empty string.
     */
    public static String joinEscaped (String[] values)
    {
        return join(values, true);
    }

    /**
     * Splits the supplied string into components based on the specified separator string.
     */
    public static String[] split (String source, String sep)
    {
        // handle the special case of a zero-component source
        if (isBlank(source)) {
            return new String[0];
        }

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

        return tokens;
    }

    /**
     * Returns an array containing the values in the supplied array converted into a table of
     * values wrapped at the specified column count and fit into the specified field width. For
     * example, a call like <code>toWrappedString(values, 5, 3)</code> might result in output like
     * so:
     *
     * <pre>
     *  12  1  9 10  3
     *   1  5  7  9 11
     *  39 15 12 80 16
     * </pre>
     */
    public static String toMatrixString (int[] values, int colCount, int fieldWidth)
    {
        StringBuilder buf = new StringBuilder();
        StringBuilder valbuf = new StringBuilder();

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

            // if we're at the end of a row but not the end of the whole integer list, append a
            // newline
            if (i % colCount == (colCount-1) &&
                i != values.length-1) {
                buf.append(LINE_SEPARATOR);
            }
        }

        return buf.toString();
    }

    /**
     * Used to convert a time interval to a more easily human readable string of the form: <code>1d
     * 15h 4m 15s 987m</code>.
     */
    public static String intervalToString (long millis)
    {
        StringBuilder buf = new StringBuilder();
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
     * Returns the class name of the supplied object, truncated to one package prior to the actual
     * class name. For example, <code>com.samskivert.util.StringUtil</code> would be reported as
     * <code>util.StringUtil</code>. If a null object is passed in, <code>null</code> is returned.
     */
    public static String shortClassName (Object object)
    {
        return (object == null) ? "null" : shortClassName(object.getClass());
    }

    /**
     * Returns the supplied class's name, truncated to one package prior to the actual class
     * name. For example, <code>com.samskivert.util.StringUtil</code> would be reported as
     * <code>util.StringUtil</code>.
     */
    public static String shortClassName (Class<?> clazz)
    {
        return shortClassName(clazz.getName());
    }

    /**
     * Returns the supplied class name truncated to one package prior to the actual class name. For
     * example, <code>com.samskivert.util.StringUtil</code> would be reported as
     * <code>util.StringUtil</code>.
     */
    public static String shortClassName (String name)
    {
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

    /**
     * Converts a name of the form <code>weAreSoCool</code> to a name of the form
     * <code>WE_ARE_SO_COOL</code>.
     */
    public static String unStudlyName (String name)
    {
        boolean seenLower = false;
        StringBuilder nname = new StringBuilder();
        int nlen = name.length();
        for (int i = 0; i < nlen; i++) {
            char c = name.charAt(i);
            // if we see an upper case character and we've seen a lower case character since the
            // last time we did so, slip in an _
            if (Character.isUpperCase(c)) {
                if (seenLower) {
                    nname.append("_");
                }
                seenLower = false;
                nname.append(c);
            } else {
                seenLower = true;
                nname.append(Character.toUpperCase(c));
            }
        }
        return nname.toString();
    }

    /**
     * See {@link #stringCode(String,StringBuilder)}.
     */
    public static int stringCode (String value)
    {
        return stringCode(value, null);
    }

    /**
     * Encodes (case-insensitively) a short English language string into a semi-unique
     * integer. This is done by selecting the first eight characters in the string that fall into
     * the set of the 16 most frequently used characters in the English language and converting
     * them to a 4 bit value and storing the result into the returned integer.
     *
     * <p> This method is useful for mapping a set of string constants to a set of unique integers
     * (e.g. mapping an enumerated type to an integer and back without having to require that the
     * declaration order of the enumerated type remain constant for all time). The caller must, of
     * course, ensure that no collisions occur.
     *
     * @param value the string to be encoded.
     * @param encoded if non-null, a string buffer into which the characters used for the encoding
     * will be recorded.
     */
    public static int stringCode (String value, StringBuilder encoded)
    {
        int code = 0;
        for (int ii = 0, uu = 0; ii < value.length(); ii++) {
            char c = Character.toLowerCase(value.charAt(ii));
            Integer cc = _letterToBits.get(c);
            if (cc == null) {
                continue;
            }
            code += cc.intValue();
            if (encoded != null) {
                encoded.append(c);
            }
            if (++uu == 8) {
                break;
            }
            code <<= 4;
        }
        return code;
    }

    /**
     * Wordwraps a string. Treats any whitespace character as a single character.
     *
     * <p>If you want the text to wrap for a graphical display, use a wordwrapping component
     * such as {@link com.samskivert.swing.Label} instead.
     *
     * @param str String to word-wrap.
     * @param width Maximum line length.
     */
    public static String wordWrap (String str, int width)
    {
        int size = str.length();
        StringBuilder buf = new StringBuilder(size + size/width);
        int lastidx = 0;
        while (lastidx < size) {
            if (lastidx + width >= size) {
                buf.append(str.substring(lastidx));
                break;
            }
            int lastws = lastidx;
            for (int ii = lastidx, ll = lastidx + width; ii < ll; ii++) {
                char c = str.charAt(ii);
                if (c == '\n') {
                    buf.append(str.substring(lastidx, ii + 1));
                    lastidx = ii + 1;
                    break;
                } else if (Character.isWhitespace(c)) {
                    lastws = ii;
                }
            }
            if (lastws == lastidx) {
                buf.append(str.substring(lastidx, lastidx + width)).append(LINE_SEPARATOR);
                lastidx += width;
            } else if (lastws > lastidx) {
                buf.append(str.substring(lastidx, lastws)).append(LINE_SEPARATOR);
                lastidx = lastws + 1;
            }
        }
        return buf.toString();
    }

    /**
     * Helper function for the various {@link #toString} methods.
     */
    protected static void toString (StringBuilder buf, Object val, String openBox, String closeBox,
                                    String sep, boolean traverseCollections)
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
            for (int i = 0; i < v.length; i++) {
                if (i > 0) {
                    buf.append(sep);
                }
                buf.append(v[i]);
            }
            buf.append(closeBox);

        } else if (val instanceof char[]) {
            buf.append(openBox);
            char[] v = (char[])val;
            for (int i = 0; i < v.length; i++) {
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
                toString(buf, v[i], openBox, closeBox, sep, traverseCollections);
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

        } else if (traverseCollections) {
            if (val instanceof Iterable<?>) {
                toString(buf, ((Iterable<?>)val).iterator(), openBox, closeBox, sep, true);

            } else if (val instanceof Iterator<?>) {
                buf.append(openBox);
                Iterator<?> iter = (Iterator<?>)val;
                for (int i = 0; iter.hasNext(); i++) {
                    if (i > 0) {
                        buf.append(sep);
                    }
                    toString(buf, iter.next(), openBox, closeBox, sep, true);
                }
                buf.append(closeBox);

            } else if (val instanceof Enumeration<?>) {
                buf.append(openBox);
                Enumeration<?> enm = (Enumeration<?>)val;
                for (int i = 0; enm.hasMoreElements(); i++) {
                    if (i > 0) {
                        buf.append(sep);
                    }
                    toString(buf, enm.nextElement(), openBox, closeBox, sep, true);
                }
                buf.append(closeBox);

            } else {
                buf.append(val);
            }

        } else {
            buf.append(val);
        }
    }

    /**
     * Helper function for the various <code>join</code> methods.
     */
    protected static String join (Object[] values, String separator, boolean escape)
    {
        StringBuilder buf = new StringBuilder();
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
     * Helper function for {@link #md5hex} and {@link #sha1hex}.
     */
    protected static byte[] digest (String codec, String source)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance(codec);
            return digest.digest(source.getBytes());
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(codec + " codec not available");
        }
    }

    /** Used to easily format floats with sensible defaults. */
    protected static final NumberFormat _ffmt = NumberFormat.getInstance();
    static {
        _ffmt.setMinimumIntegerDigits(1);
        _ffmt.setMinimumFractionDigits(1);
        _ffmt.setMaximumFractionDigits(2);
    }

    /** Used by {@link #hexlate} and {@link #unhexlate}. */
    protected static final String XLATE = "0123456789abcdef";

    /** Maps the 16 most frequent letters in the English language to a number between 0 and
     * 15. Used by {@link #stringCode}. */
    protected static final IntMap<Integer> _letterToBits = IntMaps.newHashIntMap();
    static {
        String mostCommon = "etaoinsrhldcumfp";
        for (int ii = mostCommon.length() - 1; ii >= 0; ii--) {
            _letterToBits.put(mostCommon.charAt(ii), Integer.valueOf(ii));
        }
        // sorry g, w, y, b, v, k, x, j, q, z
    }

    /** The line separator for this platform. */
    protected static String LINE_SEPARATOR = "\n";
    static {
        try {
            LINE_SEPARATOR = System.getProperty("line.separator");
        } catch (Exception e) {
        }
    }
}
