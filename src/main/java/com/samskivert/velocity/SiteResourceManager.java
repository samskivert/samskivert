//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
import com.samskivert.servlet.SiteResourceLoader;

/**
 * A resource manager implementation for Velocity that first loads site specific resources (via the
 * {@link SiteJarResourceLoader}), but falls back to default resources if no site-specific resource
 * loader is available.
 */
public class SiteResourceManager extends ResourceManagerImpl
{
    @Override
    public void initialize (RuntimeServices rsvc)
        throws Exception
    {
        super.initialize(rsvc);

        // the web framework was kind enough to slip this into the runtime when it started up
        Application app = (Application)rsvc.getApplicationAttribute(Application.VELOCITY_ATTR_KEY);
        if (app == null) {
            Log.log.warning("SiteResourceManager: No application was initialized. " +
                            "A user of the site resource manager must ensure that " +
                            "an application is instantiated and initialized.");
            return;
        }
        Log.log.info("SiteResourceManager initializing.");

        // get handles on the good stuff
        _sctx = app.getServletContext();
        _ident = app.getSiteIdentifier();

        // make sure the app has a site resource loader
        SiteResourceLoader loader = app.getSiteResourceLoader();
        if (loader == null) {
            Log.log.warning("SiteResourceManager: application must be " +
                            "configured with a site-specific resource loader " +
                            "that we can use to fetch site-specific resources.");
        }

        // create our resource loaders
        _siteLoader = new SiteJarResourceLoader(loader);
        _contextLoader = new ServletContextResourceLoader(_sctx);

        // for now, turn caching on with the expectation that new resources of any sort will result
        // in the entire web application being reloaded and clearing out the cache
        _siteLoader.setCachingOn(true);
        _contextLoader.setCachingOn(true);

        Log.log.info("SiteResourceManager initialization complete.");
    }

    @Override
    protected Resource loadResource(String resourceName, int resourceType, String encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        SiteKey skey = new SiteKey(resourceName);

        // create a blank new resource
        Resource resource = ResourceFactory.getResource(skey.path, resourceType);
        resource.setRuntimeServices(rsvc);
        resource.setEncoding(encoding);

        // first try loading it via the site-specific resource loader
        try {
            resource.setName(resourceName);
            resolveResource(resource, _siteLoader);
        } catch (ResourceNotFoundException rnfe) {
            // nothing to worry about here
        }

        // then try the servlet context loader if we didn't find a site-specific resource
        if (resource.getData() == null) {
            resource.setName(skey.path);
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
        resource.setModificationCheckInterval(loader.getModificationCheckInterval());
        resource.touch();
    }

    /** A reference to the servlet context through which we'll load default
     * resources. */
    protected ServletContext _sctx;

    /** A reference to the site identifier in use by the application. */
    protected SiteIdentifier _ident;

    /** We use this to load site-specific resources. */
    protected SiteJarResourceLoader _siteLoader;

    /** We use this to load default resources. */
    protected ServletContextResourceLoader _contextLoader;
}
