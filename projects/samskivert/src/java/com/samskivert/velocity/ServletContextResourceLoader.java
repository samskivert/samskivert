//
// $Id: ServletContextResourceLoader.java,v 1.4 2002/01/24 06:32:38 mdb Exp $
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
import javax.servlet.ServletContext;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * A Velocity resource loader that loads resources from the servlet
 * context.
 *
 * @see ServletContext#getResource
 * @see ServletContext#getResourceAsStream
 */
public class ServletContextResourceLoader extends ResourceLoader
{
    /**
     * When used with the default Velocity resource manager, we are
     * constructed with our zero-argument constructor and later
     * initialized via {@link #init}.
     */
    public ServletContextResourceLoader ()
    {
    }

    /**
     * When used with the {@link SiteResourceManager} we are constructed
     * with our servlet context reference and not later initialized.
     */
    public ServletContextResourceLoader (ServletContext sctx)
    {
        _sctx = sctx;
    }

    /**
     * Called by Velocity to initialize this resource loader.
     */
    public void init (ExtendedProperties config)
    {
        // there seems to be a tradition of logging this stuff; surely we
        // don't want to buck tradition, do we?
        rsvc.info("ServletContextResourceLoader: initialization starting.");

        // the web framework was kind enough to slip this into the runtime
        // instance when it started up
        _sctx = (ServletContext)rsvc.getApplicationAttribute("ServletContext");
        if (_sctx == null) {
            rsvc.warn("ServletContextResourceLoader: servlet context " +
                      "was not supplied as application context. A " +
                      "user of the servlet context resource loader " +
                      "must call Velocity.setApplicationAttribute(" +
                      "\"ServletContext\", getServletContext()).");
        }

        rsvc.info("ServletContextResourceLoader : initialization complete.");
    }

    /**
     * Returns the input stream that can be used to load the named
     * resource.
     *
     * @param path the path (relative to the webapp root) of resource to
     * get.
     *
     * @return an input stream that can be used to read the resource.
     *
     * @exception ResourceNotFoundException if the resource was not found.
     */
    public InputStream getResourceStream (String path)
        throws ResourceNotFoundException
    {
        // make sure we were properly initialized
        if (_sctx == null) {
            String errmsg = "ServletContextResourceLoader not properly " +
                "initialized. Can't load resources.";
            throw new ResourceNotFoundException(errmsg);
        }

        // load it on up
        InputStream stream = _sctx.getResourceAsStream(path);
        if (stream == null) {
            String errmsg = "Unable to load resource via servlet context " +
                "[path=" + path + "].";
            throw new ResourceNotFoundException(errmsg);
        }
        return stream;
    }
    
    /**
     * Things won't ever be modified when loaded from the servlet context
     * because they came from the webapp .war file and if that is
     * reloaded, everything will be thrown away and started afresh.
     */
    public boolean isSourceModified (Resource resource)
    {
        return false;
    }

    /**
     * Things won't ever be modified when loaded from the servlet context
     * because they came from the webapp .war file and if that is
     * reloaded, everything will be thrown away and started afresh. So we
     * can punt here and return zero.
     */
    public long getLastModified (Resource resource)
    {
        return 0;
    }

    /** A reference to the servlet context through which we'll load
     * things. */
    protected ServletContext _sctx;
}
