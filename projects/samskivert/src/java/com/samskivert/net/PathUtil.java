//
// $Id: PathUtil.java,v 1.1 2001/03/04 07:33:29 mdb Exp $

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
