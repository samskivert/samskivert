//
// $Id: Site.java,v 1.1 2003/09/19 02:12:55 eric Exp $

package com.samskivert.servlet.user;

import com.samskivert.util.StringUtil;

/**
 * A representation of a row in the sites table.
 */
public class Site
{
    /** The sites unqiue identifier. */
    public int siteId;

    /** The sites human readable identifier (I.e., "Shockwave") */
    public String stringId;

    /** Construct a Site record with the specified siteId. */
    public Site (int siteId)
    {
        this.siteId = siteId;
    }

    /** Construct a Site record with the specified stringId. */
    public Site (String stringId)
    {
        this.stringId = stringId;
    }

    /**
     * Constructs a blank Site record for unserialization
     * from the repository.
     */
    public Site ()
    {
    }

    public String toString () {
        return StringUtil.fieldsToString(this);
    }
}
