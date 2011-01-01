//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

import static com.samskivert.Log.log;

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
