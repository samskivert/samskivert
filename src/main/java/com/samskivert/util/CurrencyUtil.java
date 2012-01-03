//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Currency related utility functions.
 */
public class CurrencyUtil
{
    /**
     * Converts a number representing pennies to a locale-appropriate currency
     * display string using the supplied local.
     */
    public static String currencyPennies (double value, Locale locale)
    {
        return currency(value / 100.0, locale);
    }

    /**
     * Converts a number representing currency in the specified locale to a
     * displayable string.
     */
    public static String currency (double value, Locale locale)
    {
        return currency(value, NumberFormat.getCurrencyInstance(locale));
    }

    /**
     * Converts a number representing pennies to a dollars display string using
     * the US local.
     */
    public static String dollarsPennies (double value)
    {
        return currency(value / 100.0, _dollarFormatter);
    }

    /**
     * Converts a number representing dollars to a currency display string
     * using the US locale.
     */
    public static String dollars (double value)
    {
        return currency(value, _dollarFormatter);
    }

    /**
     * Converts a number representing dollars to a currency display
     * string using the supplied number format.
     */
    protected static String currency (double value, NumberFormat nformat)
    {
        return nformat.format(value);
    }

    /** A number format for formatting dollars. */
    protected static final NumberFormat _dollarFormatter =
        NumberFormat.getCurrencyInstance(Locale.US);
}
