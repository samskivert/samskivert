//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.samskivert.util.StringUtil;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * Loads Velocity templates from the classpath. Works around the problem that templates loaded from
 * a .jar file are cached for the lifetime of the VM even if a totally new class loader loads a new
 * copy of that jar file from the same path.
 */
public class ClasspathResourceLoader extends ResourceLoader
{
    @Override // from ResourceLoader
    public void init (ExtendedProperties config)
    {
    }

    @Override // from ResourceLoader
    public InputStream getResourceStream (String name)
        throws ResourceNotFoundException
    {
        if (StringUtil.isBlank(name)) {
            throw new ResourceNotFoundException ("No template name provided");
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            if (loader != null) {
                return getResourceStream(loader, name);
            }
        } catch (IOException ioe) {
            // fall through and try the system classloader
        }
        try {
            return getResourceStream(getClass().getClassLoader(), name);
        } catch (IOException ioe) {
            throw new ResourceNotFoundException("Unable to find template: " + name);
        }
    }

    @Override // from ResourceLoader
    public boolean isSourceModified (Resource resource)
    {
        return false;
    }

    @Override // from ResourceLoader
    public long getLastModified (Resource resource)
    {
        return 0;
    }

    protected InputStream getResourceStream (ClassLoader loader, String name)
        throws IOException
    {
        URL rsrc = loader.getResource(name);
        if (rsrc == null) {
            throw new FileNotFoundException(name);
        }
        URLConnection uconn = rsrc.openConnection();
        // we have to disable caching otherwise once a resource is loaded from a jar file we will
        // never get a new version of that resource even if the jar file is replaced on disk and a
        // totally separate classloader is created to read it because Java caches jar resources
        // globally in the VM based on filename only
        uconn.setUseCaches(false);
        return uconn.getInputStream();
    }
}

