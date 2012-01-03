//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
        return DEFAULT_SITE_ID;
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
