//
// $Id: FileUtil.java,v 1.4 2003/08/09 05:26:32 mdb Exp $

package com.samskivert.util;

import java.io.File;

import com.samskivert.Log;

/**
 * Utility methods for files.
 */
public class FileUtil
{
    /**
     * Recursively delete the specified directory and all files and
     * directories underneath it.
     */
    public static void recursiveDelete (File file)
    {
        recursiveWipe(file, true);
    }

    /**
     * Recursively deletes all of the files and directories in the
     * supplied directory, but not the directory itself.
     */
    public static void recursiveClean (File file)
    {
        recursiveWipe(file, false);
    }

    /**
     * Replaces <code>ext</code> with the supplied new extention if the
     * supplied file path ends in <code>ext</code>. Otherwise the new
     * extension is appended to the whole existing file path.
     */
    public static String resuffix (File file, String ext, String newext)
    {
        String path = file.getPath();
        if (path.endsWith(ext)) {
            path = path.substring(0, path.length()-ext.length());
        }
        return path + newext;
    }

    /** Helper function. */
    protected static void recursiveWipe (File file, boolean wipeMe)
    {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int ii = 0; ii < files.length; ii++) {
                recursiveWipe(files[ii], true);
            }
        }
        if (wipeMe) {
            if (!file.delete()) {
                Log.warning("Failed to delete " + file + ".");
            }
        }
    }
}
