//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Random;

/**
 * Handy functions for dealing with license key creation and general key
 * issues.
 */
public class KeyUtil
{
    /**
     * Convenient function to make a random key of the specified total
     * length, containing length - 1 random digits, that is verifiable
     * using {@link #verifyLuhn}.
     */
    public static String makeVerifiableKey (Random rand, int length)
    {
        return makeLuhnVerifiable(generateRandomKey(rand, length - 1));
    }

    /**
     * Return a key of the given length that contains random numbers.
     */
    public static String generateRandomKey (Random rand, int length)
    {
        int numKeyChars = KEY_CHARS.length();
        StringBuilder buf = new StringBuilder();
        for (int ii = 0; ii < length; ii++) {
            buf.append(KEY_CHARS.charAt(rand.nextInt(numKeyChars)));
        }
        return buf.toString();
    }

    /**
     * Verify the key with the Luhn check.  If the key is all zeros,
     * then the luhn check is true, but that is almost never what you want
     * so explicitly check for zero and return false if it is.
     */
    public static boolean verifyLuhn (String key)
    {
        // If there is a non digit, or it is all zeros.
        if (StringUtil.isBlank(key) || key.matches(".*\\D.*") ||
            key.matches("^[0]+$")) {
            return false;
        }

        return (getLuhnRemainder(key) == 0);
    }

    /**
     * Takes a random string of digits and adds the appropriate check
     * digit in order to make the whole string verifyable using {@link
     * #verifyLuhn}.
     */
    public static String makeLuhnVerifiable (String key)
    {
        String prekey = key + '0';
        int rem = getLuhnRemainder(prekey);

        return (rem == 0 ? prekey : key + (10 - rem));
    }

    /**
     * Return the luhn remainder for the given key.
     *
     * @see <a href="http://www.beachnet.com/~hstiles/cardtype.html">
     * Instructions for computing the Lunh remainder</a>
     * @see <a href="http://www.google.com/search?q=luhn%20credit%20card">
     * Find more information with Google</a>
     */
    public static int getLuhnRemainder (String key)
    {
        int len = key.length();
        int sum = 0;
        for (int multSeq = len % 2, ii=0; ii < len; ii++) {
            int c = key.charAt(ii) - '0';
            if (ii % 2 == multSeq) {
                c *= 2;
            }
            sum += (c / 10) + (c % 10);
        }

        return sum % 10;
    }

    public static void main (String[] args)
    {
        String base = "888888888888888888";
        for (int ii = 0; ii < 10; ii++) {
            System.out.println(makeLuhnVerifiable(base + ii));
        }
    }

    /** Characters used in creating key strings. */
    protected static final String KEY_CHARS = "0123456789";
}
