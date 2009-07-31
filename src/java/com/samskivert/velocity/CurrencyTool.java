//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

package com.samskivert.velocity;

import java.util.Locale;

import com.samskivert.util.CurrencyUtil;

/**
 * Provides handy currency functions for use in velocity.
 */
public class CurrencyTool
{
    /**
     * Creates a new CurrencyTool which will used the supplied request to look up the locale with
     * which to do currency formatting in.
     */
    public CurrencyTool (Locale locale)
    {
        _locale = locale;
    }

    /**
     * Converts a number representing dollars to a currency display string.
     */
    public String dollars (double value)
    {
        return CurrencyUtil.dollars(value);
    }

    /**
     * Converts a number representing pennies to a displayable dollars value.
     */
    public String dollarsPennies (double value)
    {
        return CurrencyUtil.dollarsPennies(value);
    }

    /**
     * Converts a number representing currency in the requester's locale to a display
     * string. <em>Note:</em> you probably want to be using {@link #dollars}.
     */
    public String currency (double value)
    {
        return CurrencyUtil.currency(value, _locale);
    }

    /**
     * Converts a number representing pennies to a currency display string. <em>Note:</em> you
     * probably want to be using {@link #dollarsPennies}.
     */
    public String currencyPennies (double value)
    {
        return CurrencyUtil.currencyPennies(value, _locale);
    }

    /**
     * Velocity currently doesn't support floats, so we have to provide our own support to convert
     * pennies to a dollar amount.
     */
    public String penniesToDollars (int pennies)
    {
        float decimal = pennies / 100f;
        // if the pennies is a whole number of dollars, then we return just the string number
        if (pennies % 100 == 0) {
            return String.format("%.0f", decimal);
        }
        // otherwise we always return two decimal places
        return String.format("%#.2f", decimal);
    }

    /** The locale in which we are providing currency functionality. */
    protected Locale _locale;
}
