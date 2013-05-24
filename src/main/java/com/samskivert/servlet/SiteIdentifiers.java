//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet;

import java.util.Collections;
import java.util.Iterator;

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
            public int identifySite (HttpServletRequest req) {
                return siteId;
            }
            public String getSiteString (int siteId) {
                return siteString;
            }
            public int getSiteId (String siteString) {
                return siteId;
            }
            public Iterator<Site> enumerateSites () {
                return Collections.singletonList(new Site(siteId, siteString)).iterator();
            }
        };
    }
}
