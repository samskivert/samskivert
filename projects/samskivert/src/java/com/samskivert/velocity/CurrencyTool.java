//
// $Id: CurrencyTool.java,v 1.1 2003/11/05 00:07:53 eric Exp $
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

package com.samskivert.velocity;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.util.CurrencyUtil;

/**
 * Provides handy currency functions for use in velocity.
 */
public class CurrencyTool
{
    /**
     * Creates a new CurrencyTool which will used the supplied request to
     * look up the locale with which to do currency formatting in.
     */
    public CurrencyTool (HttpServletRequest req)
    {
        _req = req;
    }

    /**
     * Converts a number representing dollars to a currency display string.
     */
    public String currency (double value)
    {
        return CurrencyUtil.currency(value, _req.getLocale());
    }

    /**
     * Converts a number representing pennies to a currency display string.
     */
    public String currencyPennies (double value)
    {
        return CurrencyUtil.currencyPennies(value, _req.getLocale());
    }

    /**
     * Velocity currently doesn't support floats, so we have to provide
     * our own support to convert pennies to a dollar amount.
     */
    public String penniesToDollars (int pennies)
    {
        return "" + (pennies / 100.0);
    }
    
    /** The servlet request we are providing currency functionality for. */
    protected HttpServletRequest _req;
}
