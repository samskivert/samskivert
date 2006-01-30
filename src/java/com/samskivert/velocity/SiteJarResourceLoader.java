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
     * @param path the path for the resource to be loaded.
     *
     * @return an input stream that can be used to read the resource.
     *
     * @exception ResourceNotFoundException if the resource was not found.
     */
    public InputStream getResourceStream (String path)
        throws ResourceNotFoundException
    {
        SiteKey skey = new SiteKey(path);

        // load it on up
        try {
            InputStream stream = _loader.getResourceAsStream(
                skey.siteId, skey.path);
            if (stream == null) {
                String errmsg = "Unable to load resource via " +
                    "site-specific jar file [path=" + path + "].";
                throw new ResourceNotFoundException(errmsg);
            }
            return stream;

        } catch (IOException ioe) {
            throw new ResourceNotFoundException(ioe.getMessage());
        }
    }
    
    /**
     * Things won't ever be modified when loaded from the servlet context
     * because they came from the webapp .war file and if that is reloaded,
     * everything will be thrown away and started afresh.
     */
    public boolean isSourceModified (Resource resource)
    {
        SiteKey skey = new SiteKey(resource.getName());

        // if the resource is for the default site, it is never considered to
        // be modified
        if (skey.siteId == SiteIdentifier.DEFAULT_SITE_ID) {
            return false;

        } else {
            // otherwise compare the last modified time of the loaded resource
            // with that of the associated site-specific jar file
            try {
                return (resource.getLastModified() <
                        _loader.getLastModified(skey.siteId));
            } catch (IOException ioe) {
                Log.warning("Failure obtaining last modified time of " +
                            "site-specific jar file [siteId=" + skey.siteId +
                            ", error=" + ioe + "].");
                return false;
            }
        }
    }

    /**
     * Things won't ever be modified when loaded from the servlet context
     * because they came from the webapp .war file and if that is reloaded,
     * everything will be thrown away and started afresh. So we can punt here
     * and return zero.
     */
    public long getLastModified (Resource resource)
    {
        SiteKey skey = new SiteKey(resource.getName());

        // if the resource is for the default site, it is never considered to
        // be modified
        if (skey.siteId == SiteIdentifier.DEFAULT_SITE_ID) {
            return 0;

        } else {
            // otherwise return the last modified time of the associated
            // site-specific jar file
            try {
                return _loader.getLastModified(skey.siteId);
            } catch (IOException ioe) {
                Log.warning("Failure obtaining last modified time of " +
                            "site-specific jar file [siteId=" + skey.siteId +
                            ", error=" + ioe + "].");
                return 0;
            }
        }
    }

    /** The site resource loader through which we'll load things. */
    protected SiteResourceLoader _loader;
}
