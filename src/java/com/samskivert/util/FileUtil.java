//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;

import com.samskivert.io.StreamUtil;

import static com.samskivert.Log.log;

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
        Enumeration<?> entries = jar.entries();
        while (!failure && entries.hasMoreElements()) {
            JarEntry entry = (JarEntry)entries.nextElement();
            File efile = new File(target, entry.getName());

            // if we're unpacking a normal jar file, it will have special path
            // entries that allow us to create our directories first
            if (entry.isDirectory()) {
                if (!efile.exists() && !efile.mkdir()) {
                    log.warning("Failed to create jar entry path [jar=" + jar +
                                ", entry=" + entry + "].");
                }
                continue;
            }

            // but some do not, so we want to ensure that our directories exist
            // prior to getting down and funky
            File parent = new File(efile.getParent());
            if (!parent.exists() && !parent.mkdirs()) {
                log.warning("Failed to create jar entry parent [jar=" + jar +
                            ", parent=" + parent + "].");
                continue;
            }

            BufferedOutputStream fout = null;
            InputStream jin = null;
            try {
                fout = new BufferedOutputStream(new FileOutputStream(efile));
                jin = jar.getInputStream(entry);
                IOUtils.copy(jin, fout);
            } catch (Exception e) {
                log.warning("Failure unpacking [jar=" + jar +
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
            log.warning("Failed to close jar file [jar=" + jar +
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
                log.warning("Failed to delete " + file + ".");
            }
        }
    }
}
