//
// $Id: ServletContextResourceManager.java,v 1.6 2004/02/25 13:16:32 mdb Exp $
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

/**
 * A resource manager implementation for Velocity that loads resources
 * from the servlet context.
 */
public class ServletContextResourceManager extends ResourceManagerImpl
{
    public void initialize (RuntimeServices rsvc)
        throws Exception
    {
        super.initialize(rsvc);
        rsvc.info("SCRM initializing.");

        // the web framework was kind enough to slip this into the runtime
        // instance when it started up
        Application app = (Application)rsvc.getApplicationAttribute(
            Application.VELOCITY_ATTR_KEY);
        if (app == null) {
            rsvc.warn("SCRM: No application was initialized. A user of the " +
                      "servlet context resource manager must ensure that " +
                      "an application is instantiated and initialized.");
        }

        // create our resource loader
        _contextLoader = new ServletContextResourceLoader(
            app.getServletContext());

        // for now, turn caching on with the expectation that new
        // resources of any sort will result in the entire web application
        // being reloaded and clearing out the cache
        _contextLoader.setCachingOn(true);

        rsvc.info("SCRM initialization complete.");
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

        resource.setResourceLoader(_contextLoader);
        resource.process();
        resource.setLastModified(_contextLoader.getLastModified(resource));
        resource.setModificationCheckInterval(
            _contextLoader.getModificationCheckInterval());
        resource.touch();

        return resource;
    }

    /** We use this to load default resources. */
    protected ServletContextResourceLoader _contextLoader;
}
