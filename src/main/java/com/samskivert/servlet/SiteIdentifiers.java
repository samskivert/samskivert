//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link SiteIdentifier} utility methods.
 */
public class SiteIdentifiers
{
    /** A site identifier that always returns the default site. */
    public static final SiteIdentifier DEFAULT = single(
        SiteIdentifier.DEFAULT_SITE_ID, SiteIdentifier.DEFAULT_SITE_STRING);

    /** Returns a site identifier that returns the specified site always. */
    public static SiteIdentifier single (final int siteId, final String siteString) {
        return new SiteIdentifier() {
            @Override public int identifySite (HttpServletRequest req) {
                return siteId;
            }
            @Override public String getSiteString (int siteId) {
                return siteString;
            }
            @Override public int getSiteId (String siteString) {
                return siteId;
            }
            @Override public Iterator<Site> enumerateSites () {
                List<Site> sites = new ArrayList<Site>();
                sites.add(new Site(siteId, siteString));
                return sites.iterator();
            }
        };
    }
}
