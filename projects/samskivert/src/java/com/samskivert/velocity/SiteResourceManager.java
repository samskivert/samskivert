//
// $Id: SiteResourceManager.java,v 1.3 2001/11/06 05:37:57 mdb Exp $
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

import javax.servlet.ServletContext;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceFactory;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import com.samskivert.Log;
import com.samskivert.servlet.SiteIdentifier;

/**
 * A resource manager implementation for Velocity that first loads site
 * specific resources (via the {@link SiteResourceLoader}), but falls back
 * to default resources if no site-specific resource loader is available.
 *
 * <p> If this resource manager is to be used, resources must be fetched
 * using {@likn SiteResourceKey} objects as keys rather than simple
 * strings.
 */
public class SiteResourceManager extends ResourceManagerImpl
{
    public void initialize (RuntimeServices rsvc)
        throws Exception
    {
        super.initialize(rsvc);

        // the web framework was kind enough to slip this into the runtime
        // instance when it started up
        Application app = (Application)rsvc.getApplicationContext();
        if (app == null) {
            rsvc.warn("SiteResourceManager: Application reference " +
                      "was not supplied as application context. A " +
                      "user of the site resource manager must supply " +
                      "a reference to the Application instance via " +
                      "Velocity.setApplicationContext().");
        }

        // get handles on the good stuff
        _sctx = app.getServletContext();
        _ident = app.getSiteIdentifier();

        // create our resource loaders
        _siteLoader = new SiteResourceLoader(_ident, _sctx);
        _contextLoader = new ServletContextResourceLoader(_sctx);

        // for now, turn caching on with the expectation that new
        // resources of any sort will result in the entire web application
        // being reloaded and clearing out the cache
        _siteLoader.setCachingOn(true);
        _contextLoader.setCachingOn(true);
    }

    protected Resource loadResource(
        Object resourceKey, int resourceType, String encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        // create a blank new resource
        Resource resource =
            ResourceFactory.getResource(resourceKey, resourceType);
        resource.setRuntimeServices(rsvc);
        resource.setKey(resourceKey);
        resource.setEncoding(encoding);

        // if the resource was requested using a site resource key, we can
        // attempt to load a site-specific version
        if (resourceKey instanceof SiteResourceKey) {
            SiteResourceKey rkey = (SiteResourceKey)resourceKey;

            // make sure the site we're loading for is not the default
            // site, in which case we want to skip to the second resource
            // loader directly
            if (rkey.getSiteId() != SiteIdentifier.DEFAULT_SITE_ID) {
                // try loading it via the site-specific resource loader
                try {
                    resolveResource(resource, _siteLoader);
                } catch (ResourceNotFoundException rnfe) {
                    // nothing to worry about here
                }
            }
        }

        // try the servlet context loader if we didn't find a
        // site-specific resource
        if (resource.getData() == null) {
            resolveResource(resource, _contextLoader);
        }

        return resource;
    }

    protected void resolveResource (Resource resource, ResourceLoader loader)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        resource.setResourceLoader(loader);
        resource.process();
        resource.setLastModified(loader.getLastModified(resource));
        resource.setModificationCheckInterval(
            loader.getModificationCheckInterval());
        resource.touch();
    }

    /** A reference to the servlet context through which we'll load
     * default resources. */
    protected ServletContext _sctx;

    /** A reference to the site identifier in use by the application. */
    protected SiteIdentifier _ident;

    /** We use this to load site-specific resources. */
    protected SiteResourceLoader _siteLoader;

    /** We use this to load default resources. */
    protected ServletContextResourceLoader _contextLoader;
}
