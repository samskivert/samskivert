//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import static com.samskivert.servlet.Log.log;

/**
 * Decodes a compound Velocity resource name plus site identifier.
 */
public class SiteKey
{
    /** The site identifier associated with this path or -1 if no site
     * identifier was specified in the path. */
    public int siteId = -1;

    /** The resource path. */
    public String path;

    public SiteKey (String path)
    {
        int cidx = path.indexOf(":");
        if (cidx == -1) {
            this.path = path;
        } else {
            try {
                siteId = Integer.parseInt(path.substring(0, cidx));
            } catch (Exception e) {
                log.warning("Invalid site path", "path", path);
            }
            this.path = path.substring(cidx+1);
        }
    }
}
