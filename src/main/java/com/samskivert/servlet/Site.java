//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
