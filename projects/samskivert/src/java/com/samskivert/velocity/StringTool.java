//
// $Id: StringTool.java,v 1.12 2003/10/23 16:09:34 eric Exp $
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

import java.text.NumberFormat;

import com.samskivert.servlet.util.HTMLUtil;
import com.samskivert.util.StringUtil;

/**
 * Provides simple string funtions like <code>blank()</code>.
 */
public class StringTool
{
    /**
     * Returns true if the supplied string is blank, false if not.
     */
    public static boolean blank (String text)
    {
        return StringUtil.blank(text);
    }

    /**
     * URL encodes the supplied text.
     */
    public static String urlEncode (String text)
    {
        return StringUtil.encode(text);
    }

    /**
     * Converts an integer to a string.
     */
    public String valueOf (int value)
    {
        return String.valueOf(value);
    }

    /**
     * Converts a number representing dollars to a currency display string.
     */
    public static String currency (double value)
    {
        return StringUtil.currency(value);
    }

    /**
     * Converts a number representing pennies to a currency display string.
     */
    public static String currencyPennies (double value)
    {
        return StringUtil.currencyPennies(value);
    }

    /**
     * Adds &lt;p&gt; tags between each pair of consecutive newlines.
     */
    public static String parafy (String text)
    {
        return HTMLUtil.makeParagraphs(text);
    }
}
