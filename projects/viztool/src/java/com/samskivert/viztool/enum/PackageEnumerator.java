//
// $Id: PackageEnumerator.java,v 1.2 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.enum;

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
        ClassEnumerator enum = new ClassEnumerator(classpath);
        String pkg = "com.samskivert.viztool.enum";
        PackageEnumerator penum = new PackageEnumerator(pkg, enum, true);

        // print out the warnings
        ClassEnumerator.Warning[] warnings = enum.getWarnings();
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
