//
// $Id: RegexpEnumerator.java,v 1.2 2003/01/28 22:39:35 mdb Exp $
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.viztool.clenum;

import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The regex enumerator filters classes based on a regular expression.
 */
public class RegexpEnumerator extends FilterEnumerator
{
    public RegexpEnumerator (String regexp, String exregex, Iterator source)
        throws PatternSyntaxException
    {
        super(source);
        _regexp = Pattern.compile(regexp);
        if (exregex != null) {
            _exreg = Pattern.compile(exregex);
        }
    }

    protected boolean filterClass (String clazz)
    {
        return !(_regexp.matcher(clazz).matches() &&
                 (_exreg == null || !_exreg.matcher(clazz).matches()));
    }

    protected Pattern _regexp;
    protected Pattern _exreg;
}
