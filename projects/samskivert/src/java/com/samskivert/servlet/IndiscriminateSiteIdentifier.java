//
// $Id: IndiscriminateSiteIdentifier.java,v 1.2 2001/11/06 04:48:08 mdb Exp $
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

import javax.servlet.http.HttpServletRequest;

/**
 * Used by default and for systems that have no need to discriminate
 * between different sites in their web applications.
 */
public class IndiscriminateSiteIdentifier implements SiteIdentifier
{
    /**
     * Always returns {@link #DEFAULT_SITE_ID} regardless of the
     * information in the request.
     */
    public int identifySite (HttpServletRequest req)
    {
        return DEFAULT_SITE_ID;
    }

    /**
     * Always returns {@link #DEFAULT_SITE_STRING} regardless of the value
     * of the supplied identifer.
     */
    public String getSiteString (int siteId)
    {
        return DEFAULT_SITE_STRING;
    }
}
