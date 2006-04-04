//
// $Id: FileUtil.java,v 1.4 2003/08/09 05:26:32 mdb Exp $

package com.samskivert.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.CopyUtils;

import com.samskivert.Log;
import com.samskivert.io.StreamUtil;

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

    /**
     * Unpacks the specified jar file intto the specified target directory.
     *
     * @return true if the jar file was successfully unpacked, false if an
     * error occurred that prevented the unpacking. The error will be logged.
     */
    public static boolean unpackJar (JarFile jar, File target)
    {
        boolean failure = false;
        Enumeration entries = jar.entries();
        while (!failure && entries.hasMoreElements()) {
            JarEntry entry = (JarEntry)entries.nextElement();
            File efile = new File(target, entry.getName());

            // if we're unpacking a normal jar file, it will have special path
            // entries that allow us to create our directories first
            if (entry.isDirectory()) {
                if (!efile.exists() && !efile.mkdir()) {
                    Log.warning("Failed to create jar entry path [jar=" + jar +
                                ", entry=" + entry + "].");
                }
                continue;
            }

            // but some do not, so we want to ensure that our directories exist
            // prior to getting down and funky
            File parent = new File(efile.getParent());
            if (!parent.exists() && !parent.mkdirs()) {
                Log.warning("Failed to create jar entry parent [jar=" + jar +
                            ", parent=" + parent + "].");
                continue;
            }

            BufferedOutputStream fout = null;
            InputStream jin = null;
            try {
                fout = new BufferedOutputStream(new FileOutputStream(efile));
                jin = jar.getInputStream(entry);
                CopyUtils.copy(jin, fout);
            } catch (Exception e) {
                Log.warning("Failure unpacking [jar=" + jar +
                            ", entry=" + efile + ", error=" + e + "].");
                failure = true;
            } finally {
                StreamUtil.close(jin);
                StreamUtil.close(fout);
            }
        }

        try {
            jar.close();
        } catch (Exception e) {
            Log.warning("Failed to close jar file [jar=" + jar +
                        ", error=" + e + "].");
        }

        return !failure;
    }

    /** Helper function. */
    protected static void recursiveWipe (File file, boolean wipeMe)
    {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int ii = 0; ii < files.length; ii++) {
                    recursiveWipe(files[ii], true);
                }
            }
        }
        if (wipeMe) {
            if (!file.delete()) {
                Log.warning("Failed to delete " + file + ".");
            }
        }
    }
}
