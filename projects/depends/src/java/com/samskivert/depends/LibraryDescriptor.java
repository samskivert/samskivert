//
// $Id: LibraryDescriptor.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends;

import java.net.URL;

import java.util.List;

/**
 * Describes a library.
 */
public class LibraryDescriptor
{
    public String getLibraryName ()
    {
        return _libraryName;
    }

    public String getCopyright ()
    {
        return _copyright;
    }

    public String getContact ()
    {
        return _contact;
    }

    public URL getLibraryURL ()
    {
        return _libraryURL;
    }

    public URL getDescriptorURL ()
    {
        return _descriptorURL;
    }

    public List getReleases ()
    {
        return _releases;
    }

    public Release getLatestRelease ()
    {
        return null;
    }

    protected String _libraryName;

    protected String _copyright;

    protected String _contact;

    protected URL _libraryURL;

    protected URL _descriptorURL;

    protected List _releases;
}
