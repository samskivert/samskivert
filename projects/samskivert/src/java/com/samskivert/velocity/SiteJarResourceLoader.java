//
// $Id: SiteJarResourceLoader.java,v 1.3 2004/02/25 13:16:32 mdb Exp $
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

import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import com.samskivert.Log;
import com.samskivert.servlet.SiteIdentifier;
import com.samskivert.servlet.SiteResourceLoader;

/**
 * A Velocity resource loader that loads resources from site-specific jar
 * files. This cannot be used with the default Velocity resource manager,
 * but is used by the {@link SiteResourceManager} which automatically
 * handles the creation and use of this resource loader.
 *
 * @see SiteResourceLoader
 */
public class SiteJarResourceLoader extends ResourceLoader
{
    /**
     * Constructs a site resource loader that will use the specified site
     * identifier to map site ids to site strings.
     */
    public SiteJarResourceLoader (SiteResourceLoader loader)
    {
        // we'll use this to load resources
        _loader = loader;
    }

    /**
     * This is not called.
     */
    public void init (ExtendedProperties config)
    {
    }

    /**
     * Returns the input stream that can be used to load the named
     * resource.
     *
     * @param resourceKey the locator key for the resource to be loaded.
     *
     * @return an input stream that can be used to read the resource.
     *
     * @exception ResourceNotFoundException if the resource was not found.
     */
    public InputStream getResourceStream (Object resourceKey)
        throws ResourceNotFoundException
    {
        SiteResourceKey skey = castKey(resourceKey);
        if (skey == null) {
            String errmsg = "Cannot use SiteResourceLoader without " +
                "loading resources with SiteResourceKey instances.";
            throw new ResourceNotFoundException(errmsg);
        }

        // load it on up
        try {
            InputStream stream = _loader.getResourceAsStream(
                skey.getSiteId(), skey.getPath());
            if (stream == null) {
                String errmsg = "Unable to load resource via " +
                    "site-specific jar file [key=" + skey + "].";
                throw new ResourceNotFoundException(errmsg);
            }
            return stream;

        } catch (IOException ioe) {
            throw new ResourceNotFoundException(ioe.getMessage());
        }
    }
    
    /**
     * Things won't ever be modified when loaded from the servlet context
     * because they came from the webapp .war file and if that is
     * reloaded, everything will be thrown away and started afresh.
     */
    public boolean isSourceModified (Resource resource)
    {
        SiteResourceKey skey = castKey(resource.getKey());
        if (skey == null) {
            // return false if we were supplied with a bogus key
            return false;
        }

        // if the resource is for the default site, it is never considered
        // to be modified
        int siteId = skey.getSiteId();
        if (siteId == SiteIdentifier.DEFAULT_SITE_ID) {
            return false;

        } else {
            // otherwise compare the last modified time of the loaded
            // resource with the last modified time of the associated
            // site-specific jar file
            try {
                return (resource.getLastModified() <
                        _loader.getLastModified(siteId));
            } catch (IOException ioe) {
                Log.warning("Failure obtaining last modified time of " +
                            "site-specific jar file [siteId=" + siteId +
                            ", error=" + ioe + "].");
                return false;
            }
        }
    }

    /**
     * Things won't ever be modified when loaded from the servlet context
     * because they came from the webapp .war file and if that is
     * reloaded, everything will be thrown away and started afresh. So we
     * can punt here and return zero.
     */
    public long getLastModified (Resource resource)
    {
        SiteResourceKey skey = castKey(resource.getKey());
        if (skey == null) {
            // return 0 if we were supplied with a bogus key
            return 0;
        }

        // if the resource is for the default site, it is never considered
        // to be modified
        int siteId = skey.getSiteId();
        if (siteId == SiteIdentifier.DEFAULT_SITE_ID) {
            return 0;

        } else {
            // otherwise return the last modified time of the associated
            // site-specific jar file
            try {
                return _loader.getLastModified(siteId);
            } catch (IOException ioe) {
                Log.warning("Failure obtaining last modified time of " +
                            "site-specific jar file [siteId=" + siteId +
                            ", error=" + ioe + "].");
                return 0;
            }
        }
    }

    /**
     * Casts the supplied resource key to a {@link SiteResourceKey} and
     * returns it iff it is an instance of such. Returns null otherwise.
     */
    protected SiteResourceKey castKey (Object resourceKey)
    {
        return (resourceKey instanceof SiteResourceKey) ?
            (SiteResourceKey)resourceKey : null;
    }

    /** A reference to the site resource loader through which we'll load
     * things. */
    protected SiteResourceLoader _loader;
}
