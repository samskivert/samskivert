//
// $Id: IndiscriminateSiteIdentifier.java,v 1.3 2003/11/13 00:53:00 mdb Exp $
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

package com.samskivert.servlet;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

/**
 * Used by default and for systems that have no need to discriminate between different sites in
 * their web applications.
 */
public class IndiscriminateSiteIdentifier implements SiteIdentifier
{
    /**
     * Always returns {@link #DEFAULT_SITE_ID} regardless of the information in the request.
     */
    public int identifySite (HttpServletRequest req)
    {
        // check whether our site id was overridden
        Integer override = (Integer)req.getAttribute(SITE_ID_OVERRIDE_KEY);
        if (override != null) {
            return override;
        } else {
            return DEFAULT_SITE_ID;
        }
    }

    /**
     * Always returns {@link #DEFAULT_SITE_STRING} regardless of the value of the supplied
     * identifer.
     */
    public String getSiteString (int siteId)
    {
        return DEFAULT_SITE_STRING;
    }

    /**
     * Always returns {@link #DEFAULT_SITE_ID} regardless of the value of the supplied string.
     */
    public int getSiteId (String siteString)
    {
        return DEFAULT_SITE_ID;
    }

    // documented inherited from interface
    public Iterator<Site> enumerateSites ()
    {
        return _sites.iterator();
    }

    protected static ArrayList<Site> _sites = new ArrayList<Site>();
    static {
        _sites.add(new Site(DEFAULT_SITE_ID, DEFAULT_SITE_STRING));
    }
}
