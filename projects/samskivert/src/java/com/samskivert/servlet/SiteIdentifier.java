//
// $Id: SiteIdentifier.java,v 1.1 2001/10/31 23:38:37 mdb Exp $
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
 * Responsible for determining the unique site identifier based on
 * information available in the HTTP request. Because the site identifier
 * implementation is likely to have access to the site classification
 * metadata, this interface is also used to map integer site identifiers
 * to string site identifiers.
 */
public interface SiteIdentifier
{
    /** The default site identifier, to be used when a site cannot be
     * identified or for site identifiers that don't wish to distinguish
     * between sites. */
    public static final int DEFAULT_SITE_ID = -1;

    /** The string identifier for the default site. */
    public static final String DEFAULT_SITE_STRING = "default";

    /**
     * Returns the unique identifier for the site on which this request
     * originated. That may be divined by looking at the server name, or
     * perhaps a request parameter, or part of the path info. The
     * mechanism (or mechanisms) are up to the implementation.
     *
     * @param req the http servlet request the site for which we are
     * trying to identify.
     *
     * @return the unique site identifier requestsed or {@link
     * #DEFAULT_SITE_ID} if the site could not be identified.
     */
    public int identifySite (HttpServletRequest req);

    /**
     * Returns a string representation of the site identifier. The site
     * identifier in use can map the site ids to strings however it likes
     * as long as it consistently maps the same identifier to the same
     * string. Presumably these strings would be human readable and
     * meaningful.
     *
     * @param siteId the unique integer identifier for the site that we
     * wish to be identified by a string.
     *
     * @return the string identifier for this site.
     */
    public String getSiteString (int siteId);
}
