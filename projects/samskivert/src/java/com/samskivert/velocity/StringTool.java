//
// $Id: StringTool.java,v 1.8 2003/10/02 02:22:21 ray Exp $
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

import java.net.URLEncoder;
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
    public boolean blank (String text)
    {
        return StringUtil.blank(text);
    }

    /**
     * URL encodes the supplied text.
     */
    public String urlEncode (String text)
    {
        return URLEncoder.encode(text);
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
    public String currency (double value)
    {
        return _curFormatter.format(value);
    }

    /**
     * Converts a number representing pennies to a currency display string.
     */
    public String currencyPennies (double value)
    {
        return currency(value / 100.0);
    }

    /**
     * Adds &lt;p&gt; tags between each pair of consecutive newlines.
     */
    public String parafy (String text)
    {
        return HTMLUtil.makeParagraphs(text);
    }

    protected static NumberFormat _curFormatter =
        NumberFormat.getCurrencyInstance();
}
