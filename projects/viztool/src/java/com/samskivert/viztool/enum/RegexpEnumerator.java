//
// $Id: RegexpEnumerator.java,v 1.1 2001/12/03 08:34:53 mdb Exp $
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

package com.samskivert.viztool.enum;

import java.util.Iterator;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * The regex enumerator filters classes based on a regular expression.
 */
public class RegexpEnumerator extends FilterEnumerator
{
    public RegexpEnumerator (String regexp, Iterator source)
        throws RESyntaxException
    {
        super(source);
        _regexp = new RE(regexp);
    }

    protected boolean filterClass (String clazz)
    {
        return !_regexp.match(clazz);
    }

    protected RE _regexp;
}
