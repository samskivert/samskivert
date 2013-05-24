//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

// we can't static import log because ResourceLoader defines a log that shadows it
import com.samskivert.servlet.Log;
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
    @Override
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
    @Override
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
    @Override
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
                Log.log.warning("Failure obtaining last modified time of site-specific jar file",
                                "siteId", skey.siteId, "error", ioe);
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
    @Override
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
                Log.log.warning("Failure obtaining last modified time of site-specific jar file",
                                "siteId", skey.siteId, "error", ioe);
                return 0;
            }
        }
    }

    /** The site resource loader through which we'll load things. */
    protected SiteResourceLoader _loader;
}
