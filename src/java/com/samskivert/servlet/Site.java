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

package com.samskivert.servlet;

import com.samskivert.util.StringUtil;

/**
 * Represents a site mapping known to a {@link SiteIdentifier}.
 *
 * @see SiteIdentifier#enumerateSites
 */
public class Site
{
    /** The site's unqiue identifier. */
    public int siteId;

    /** The site's human readable identifier (i.e., "monkeybutter"). */
    public String siteString;

    /** Constructs a site record with the specified id and string. */
    public Site (int siteId, String siteString)
    {
        this.siteId = siteId;
        this.siteString = siteString;
    }

    /**
     * Constructs a blank record for unserialization from the repository.
     */
    public Site ()
    {
    }

    @Override
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
