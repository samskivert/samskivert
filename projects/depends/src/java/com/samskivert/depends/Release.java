//
// $Id: Release.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends;

import java.net.URL;

import java.util.Date;
import java.util.List;

/**
 * Describes a particular release of a library.
 */
public class Release
{
    public Version getVersion ()
    {
        return _version;
    }

    public Date getDate ()
    {
        return _date;
    }

    public URL getJarURL ()
    {
        return _jarURL;
    }

    public List getDependencies ()
    {
        return _dependencies;
    }

    protected Version _version;

    protected Date _date;

    protected URL _jarURL;

    protected List _dependencies;
}
