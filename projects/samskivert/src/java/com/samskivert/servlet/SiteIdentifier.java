//
// $Id: SiteIdentifier.java,v 1.3 2003/11/13 00:53:00 mdb Exp $
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

import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

/**
 * Responsible for determining the unique site identifier based on
 * information available in the HTTP request. Site identifiers are
 * integers ranging from 1 to {@link Integer#MAX_VALUE}. Because the site
 * identifier implementation is likely to have access to the site
 * classification metadata, this interface is also used to map integer
 * site identifiers to string site identifiers.
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
     * #DEFAULT_SITE_ID} if the site could not be identified. No site
     * should ever have a site id value of 0.
     */
    public int identifySite (HttpServletRequest req);

    /**
     * Returns a string representation of the site identifier. The
     * SiteIdentifier in use can map the site ids to strings however it
     * likes as long as it consistently maps the same identifier to the
     * same string and vice versa. Presumably these strings would be human
     * readable and meaningful.
     *
     * @param siteId the unique integer identifier for the site that we
     * wish to be identified by a string.
     *
     * @return the string identifier for this site.
     */
    public String getSiteString (int siteId);

    /**
     * Returns the site identifier for the site associated with the
     * supplied site string. The SiteIdentifier in use can map the site
     * ids to strings however it likes as long as it consistently maps the
     * same string to the same identifier and vice versa.
     *
     * @param siteString the string to be converted into a site identifer.
     *
     * @return the integer identifier for this site.
     */
    public int getSiteId (String siteString);

    /**
     * Returns an enumerator over all {@link Site} mappings known to this
     * SiteIdentifier.
     */
    public Iterator enumerateSites ();
}
