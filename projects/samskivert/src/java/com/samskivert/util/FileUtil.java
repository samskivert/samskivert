//
// $Id: FileUtil.java,v 1.2 2003/05/23 18:13:36 mdb Exp $

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
        recursiveWipe(file, false);
    }

    /**
     * Recursively deletes all of the files and directories in the
     * supplied directory, but not the directory itself.
     */
    public static void recursiveClean (File file)
    {
        recursiveWipe(file, false);
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
