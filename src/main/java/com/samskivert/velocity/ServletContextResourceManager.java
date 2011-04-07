//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceFactory;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;

/**
 * A resource manager implementation for Velocity that loads resources from the servlet context.
 */
public class ServletContextResourceManager extends ResourceManagerImpl
{
    @Override
    public void initialize (RuntimeServices rsvc)
        throws Exception
    {
        super.initialize(rsvc);

        // the web framework was kind enough to slip this into the runtime when it started up
        Application app = (Application)rsvc.getApplicationAttribute(Application.VELOCITY_ATTR_KEY);
        if (app == null) {
            rsvc.getLog().warn("SCRM: No application was initialized. A user of the " +
                               "servlet context resource manager must ensure that " +
                               "an application is instantiated and initialized.");
            return;
        }
        rsvc.getLog().info("SCRM initializing.");

        // create our resource loader
        _contextLoader = new ServletContextResourceLoader(app.getServletContext());

        // for now, turn caching on with the expectation that new resources of any sort will result
        // in the entire web application being reloaded and clearing out the cache
        _contextLoader.setCachingOn(true);

        rsvc.getLog().info("SCRM initialization complete.");
    }

    @Override
    protected Resource loadResource(String resourceName, int resourceType, String encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        // create a blank new resource
        Resource resource = ResourceFactory.getResource(resourceName, resourceType);
        resource.setRuntimeServices(rsvc);
        resource.setName(resourceName);
        resource.setEncoding(encoding);

        resource.setResourceLoader(_contextLoader);
        resource.process();
        resource.setLastModified(_contextLoader.getLastModified(resource));
        resource.setModificationCheckInterval(_contextLoader.getModificationCheckInterval());
        resource.touch();

        return resource;
    }

    /** We use this to load default resources. */
    protected ServletContextResourceLoader _contextLoader;
}
