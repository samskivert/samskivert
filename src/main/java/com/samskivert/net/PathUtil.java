//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.net;

import java.io.File;
import java.io.IOException;

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

    /**
     * Gets the individual path elements building up the canonical path to the given file.
     */
    public static String[] getCanonicalPathElements (File file)
        throws IOException
    {
        file = file.getCanonicalFile();

        // If we were a file, get its parent
        if (!file.isDirectory()) {
            file = file.getParentFile();
        }

        return file.getPath().split(File.separator);
    }

    /**
     * Computes a relative path between to Files
     *
     * @param file the file we're referencing
     * @param relativeTo the path from which we want to refer to the file
     */
    public static String computeRelativePath (File file, File relativeTo)
        throws IOException
    {
        String[] realDirs = getCanonicalPathElements(file);
        String[] relativeToDirs = getCanonicalPathElements(relativeTo);

        // Eliminate the common root
        int common = 0;
        for (; common < realDirs.length && common < relativeToDirs.length; common++) {
            if (!realDirs[common].equals(relativeToDirs[common])) {
                break;
            }
        }

        String relativePath = "";

        // For each remaining level in the file path, add a ..
        for (int ii = 0; ii < (realDirs.length - common); ii++) {
            relativePath += ".." + File.separator;
        }

        // For each level in the resource path, add the path
        for (; common < relativeToDirs.length; common++) {
            relativePath += relativeToDirs[common] + File.separator;
        }

        return relativePath;
    }
}
