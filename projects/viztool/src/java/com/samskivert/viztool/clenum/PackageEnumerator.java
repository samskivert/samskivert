//
// $Id: PackageEnumerator.java,v 1.3 2001/08/12 04:36:57 mdb Exp $
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

import java.util.Iterator;

/**
 * The package enumerator filters out only classes from the specified
 * package from the class enumerator provided at construct time.
 */
public class PackageEnumerator extends FilterEnumerator
{
    public PackageEnumerator (String pkg, Iterator source, boolean subpkgs)
    {
        super(source);
        _package = pkg;
        _subpkgs = subpkgs;
    }

    protected boolean filterClass (String clazz)
    {
        if (!clazz.startsWith(_package)) {
            return true;
        }

        return _subpkgs ? false:
            (clazz.substring(_package.length()+1).indexOf(".") != -1);
    }

    public static void main (String[] args)
    {
        // run ourselves on the classpath
        String classpath = System.getProperty("java.class.path");
        ClassEnumerator clenum = new ClassEnumerator(classpath);
        String pkg = "com.samskivert.viztool.clenum";
        PackageEnumerator penum = new PackageEnumerator(pkg, clenum, true);

        // print out the warnings
        ClassEnumerator.Warning[] warnings = clenum.getWarnings();
        for (int i = 0; i < warnings.length; i++) {
            System.out.println("Warning: " + warnings[i].reason);
        }

        // enumerate over whatever classes match our package
        while (penum.hasNext()) {
            System.out.println("Class: " + penum.next());
        }
    }

    protected String _package;
    protected boolean _subpkgs;
}
