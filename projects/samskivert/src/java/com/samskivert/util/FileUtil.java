//
// $Id: FileUtil.java,v 1.1 2003/05/03 00:50:57 ray Exp $

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
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int ii = 0; ii < files.length; ii++) {
                recursiveDelete(files[ii]);
            }
        }
        if (!file.delete()) {
            Log.warning("Failed to delete " + file + ".");
        }
    }
}
