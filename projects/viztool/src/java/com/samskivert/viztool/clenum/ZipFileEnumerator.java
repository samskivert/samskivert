//
// $Id: ZipFileEnumerator.java,v 1.2 2001/08/12 04:36:57 mdb Exp $
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.viztool.clenum;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.*;

import com.samskivert.viztool.Log;

/**
 * The zip file enumerator enumerates all of the classes in a .zip class
 * archive.
 */
public class ZipFileEnumerator extends ComponentEnumerator
{
    /**
     * Constructs a prototype enumerator that can be used for matching.
     */
    public ZipFileEnumerator ()
    {
    }

    /**
     * Constructs a zip file enumerator with the specified zip file for
     * enumeration.
     */
    public ZipFileEnumerator (String zippath)
        throws EnumerationException
    {
        try {
            _zipfile = new ZipFile(zippath);
            _entenum = _zipfile.entries();
            scanToNextClass();

        } catch (IOException ioe) {
            String msg = "Can't enumerate zip file '" + zippath + "': " +
                ioe.getMessage();
            throw new EnumerationException(msg);
        }
    }

    // documentation inherited from interface
    public boolean matchesComponent (String component)
    {
        return component.endsWith(ZIP_SUFFIX);
    }

    // documentation inherited from interface
    public ComponentEnumerator enumerate (String component)
        throws EnumerationException
    {
        return new ZipFileEnumerator(component);
    }

    // documentation inherited from interface
    public boolean hasMoreClasses ()
    {
        return (_nextClass != null);
    }

    // documentation inherited from interface
    public String nextClass ()
    {
        String clazz = _nextClass;
        _nextClass = null;
        scanToNextClass();
        return clazz;
    }

    protected void scanToNextClass ()
    {
        // if we've already scanned to the end of our zipfile, we can bail
        // immediately
        if (_zipfile == null) {
            return;
        }

        // otherwise scan through the zip contents for the next thing that
        // looks like a class
        while (_entenum.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)_entenum.nextElement();
            String nextClass = entry.getName();
            if (nextClass.endsWith(CLASS_SUFFIX)) {
                _nextClass = pathToClassName(nextClass);
                break;
            }
        }

        // if we've reached the end of the zip file, we want to close
        // things up
        if (_zipfile != null && _nextClass == null) {
            try {
                _zipfile.close();
            } catch (IOException ioe) {
                Log.warning("Error closing archive: " + ioe.getMessage());
            }
            _zipfile = null;
        }
    }

    protected ZipFile _zipfile;
    protected Enumeration _entenum;
    protected String _nextClass;

    protected static final String ZIP_SUFFIX = ".zip";
}
