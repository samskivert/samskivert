//
// $Id: DirectoryEnumerator.java,v 1.2 2001/08/12 04:36:57 mdb Exp $
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

import java.io.*;
import java.util.ArrayList;

import com.samskivert.util.StringUtil;
import com.samskivert.viztool.Log;

/**
 * The directory enumerator enumerates all of the classes in a directory
 * hierarchy.
 */
public class DirectoryEnumerator extends ComponentEnumerator
{
    /**
     * Constructs a prototype enumerator that can be used for matching.
     */
    public DirectoryEnumerator ()
    {
    }

    /**
     * Constructs a directory enumerator with the specified root directory
     * for enumeration.
     */
    public DirectoryEnumerator (String dirpath)
        throws EnumerationException
    {
        _root = new File(dirpath);
        _rootpath = _root.getAbsolutePath();

        // make sure the specified component exists
        if (!_root.exists()) {
            String msg = "Can't enumerate '" + dirpath +
                "': directory doesn't exist";
            throw new EnumerationException(msg);
        }

        // make sure the specified component is a directory
        if (!_root.isDirectory()) {
            String msg = "Can't enumerate non-directory '" + dirpath + "'.";
            throw new EnumerationException(msg);
        }

        // create a directory record for our root directory
        addDirectory(_root);

        // and scan to the first class
        scanToNextClass();
    }

    // documentation inherited from interface
    public boolean matchesComponent (String component)
    {
        // the directory enumerator picks up anything that falls through
        // the zip or jar file enumerators
        return true;
    }

    // documentation inherited from interface
    public ComponentEnumerator enumerate (String component)
        throws EnumerationException
    {
        return new DirectoryEnumerator(component);
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
        // if we have no drecords, we have nothing to do
        while (_drecords.size() > 0) {
            // grab a reference to the last drecord in the list
            DirRecord rec = (DirRecord)_drecords.get(_drecords.size()-1);

            // grab the file related to our current position
            File target = rec.kids[rec.kidpos];

            // bump up the kidpos so that things are in place for the next
            // iteration; if we just grabbed the last kid, pop this record
            // off of the stack
            if (++rec.kidpos >= rec.kids.length) {
                _drecords.remove(_drecords.size()-1);
            }

            // if our target file is a directory, push another drecord
            // onto the stack and recurse into it
            if (target.isDirectory()) {
                addDirectory(target);
                continue;
            }

            // otherwise, process it as a file
            String path = target.getAbsolutePath();

            // make sure it's readable
            if (!target.canRead()) {
                Log.warning("Can't read file '" + path + "'.");
                continue;
            }

            // check to see if it matches our filename pattern
            if (path.endsWith(CLASS_SUFFIX)) {
                // strip off the root path (plus one for the trailing slash)
                path = path.substring(_rootpath.length()+1);
                _nextClass = pathToClassName(path);
                return;
            }

            // if we didn't match, we loop back through and process the
            // next kid (potentially popping back up the directory record
            // stack in the process)
        }
    }

    protected void addDirectory (File dir)
    {
        DirRecord rec = new DirRecord(dir);
        if (rec.kids == null) {
            String path = dir.getAbsolutePath();
            // complain if there was an error reading the directory
            Log.warning("Unable to scan directory '" + path + "'.");

        } else if (rec.kids.length > 0) {
            // only add the record if the directory actually has some
            // children that we can scan
            _drecords.add(rec);
        }
    }

    protected static class DirRecord
    {
        public File directory;
        public File[] kids;
        public int kidpos;

        public DirRecord (File directory)
        {
            this.directory = directory;
            kids = directory.listFiles();
        }
    }

    protected File _root;
    protected String _rootpath;
    protected ArrayList _drecords = new ArrayList();
    protected String _nextClass;
}
