//
// $Id: PathUtil.java,v 1.3 2001/08/13 23:24:24 mdb Exp $
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

package com.samskivert.net;

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
     * path, the entire path will be replaced. Note that this function is
     * intended for use on URLs rather than filesystem paths and thus
     * always uses forward slash rather than the platform defined path
     * separator.
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

    /**
     * Appends the supplied affix to the specified source path, ensuring
     * that exactly one path separator (<code>/</code> as we are dealing
     * with URLs here not platform specific file-system paths) is used
     * between the two. <em>Note:</em> this means that the affix will be
     * made into a relative path regardless of whether or not it starts
     * with a <code>/</code>.
     */
    public static String appendPath (String source, String affix)
    {
        if (source.endsWith("/")) {
            if (affix.startsWith("/")) {
                return source + affix.substring(1);
            } else {
                return source + affix;
            }
        } else if (affix.startsWith("/")) {
            return source + affix;
        } else {
            return source + "/" + affix;
        }
    }
}
