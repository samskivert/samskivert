//
// $Id: SiteResourceKey.java,v 1.1 2001/11/06 04:49:32 mdb Exp $
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

package com.samskivert.velocity;

import com.samskivert.servlet.SiteIdentifier;
import com.samskivert.util.IntListUtil;
import com.samskivert.util.StringUtil;

/**
 * The site resource key is used as a key for resources that can be used
 * for one or more sites. A resource always has the same path, but can be
 * valid for a single site, or multiple sites and the set of sites for
 * which a resource is valid can change without impacting the key's hash
 * value.
 */
public class SiteResourceKey
{
    /**
     * Constructs a new site resource key for the specified resource,
     * loaded via the specified site.
     */
    public SiteResourceKey (int siteId, String path)
    {
        _path = path;
        _sites = new int[] { siteId };
    }

    /**
     * Resource keys that contain only a single site id will return that
     * site id as their primary. Those that contain multiple sites are
     * assumed to reference default resources (which may be applicable to
     * multiple sites) in which case the default site id is returned.
     */
    public int getSiteId ()
    {
        return (_sites.length == 1 ? _sites[0] :
                SiteIdentifier.DEFAULT_SITE_ID);
    }

    /**
     * Returns the path to the resource referenced by this site resource
     * key.
     */
    public String getPath ()
    {
        return _path;
    }

    /**
     * Returns true if this resource key contains the specified site in
     * its site set.
     */
    public boolean containsSite (int siteId)
    {
        return IntListUtil.contains(_sites, siteId);
    }

    /**
     * Adds a site to this site resource key. The resource to which this
     * key maps is assumed to be valid for the added site.
     */
    public void addSite (int siteId)
    {
        // sanity check
        if (siteId == 0) {
            throw new IllegalArgumentException(
                "Site IDs cannot have the value 0.");
        }

        // add the site to the list only if it's not already there
        int[] sites = IntListUtil.testAndAdd(_sites, siteId);
        if (sites != null) {
            _sites = sites;
        }
    }

    /**
     * Removes a site from this site resource key. The resource to which
     * this key maps is no longer assumed to be valid for the removed
     * site.
     */
    public boolean removeSite (int siteId)
    {
        // sanity check
        if (siteId == 0) {
            throw new IllegalArgumentException(
                "Site IDs cannot have the value 0.");
        }

        // remove the site from the list
        return (IntListUtil.remove(_sites, siteId) != 0);
    }

    /**
     * We hash to the hash value of our path.
     */
    public int hashCode ()
    {
        return _path.hashCode();
    }

    /**
     * Two site resource keys are equal if the paths are the same and at
     * least one site id overlaps between the two keys. Most equality
     * comparisons will involve one multi-site key and one key that
     * contains only a single site.
     */
    public boolean equals (Object other)
    {
        if (other instanceof SiteResourceKey) {
            SiteResourceKey okey = (SiteResourceKey)other;

            // make sure the paths match
            if (!_path.equals(okey._path)) {
                return false;
            }

            // select the key with fewer sites in its sites array to be in
            // the outer loop
            int[] s1 = _sites;
            int[] s2 = okey._sites;
            if (s2.length < s1.length) {
                s1 = s2;
                s2 = _sites;
            }

            for (int i1 = 0; i1 < s1.length; i1++) {
                for (int i2 = 0; i2 < s2.length; i2++) {
                    if (s1[i1] == s2[i2]) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * The string representation of a resource key is just the path so
     * that it is backwards compatible with older resource loaders.
     */
    public String toString ()
    {
        return _path;
    }

    /**
     * Returns a string description of this object.
     */
    public String description ()
    {
        return ("[path=" + _path +
                ", sites=" + StringUtil.toString(_sites) + "]");
    }

    /** The path to the resource. */
    protected String _path;

    /** The sites for which this resource is valid. */
    protected int[] _sites;
}
