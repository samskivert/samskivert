//
// $Id: PathUtil.java,v 1.2 2001/08/11 22:43:29 mdb Exp $
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

package com.samskivert.util;

/**
 * Path related utility functions.
 */
public class PathUtil
{
    /**
     * Replaces the final component in the supplied path with the
     * specified new component. For example, if <code>/foo/bar/baz</code>
     * was provided as the source path, <code>baz</code> would be replaced
     * with the supplied new path component. If no slashes occur in the
     * path, the entire path will be replaced.
     */
    public static String replaceFinalComponent (String source,
                                                String newComponent)
    {
        int sidx = source.lastIndexOf("/");
        if (sidx != -1) {
            return source.substring(0, sidx+1) + newComponent;
        } else {
            return newComponent;
        }
    }
}
