//
// $Id: Site.java,v 1.2 2003/11/13 00:53:00 mdb Exp $

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

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
