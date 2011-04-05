//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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
