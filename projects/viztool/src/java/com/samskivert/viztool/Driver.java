//
// $Id: Driver.java,v 1.1 2001/07/14 00:55:21 mdb Exp $

package com.samskivert.viztool;

import com.samskivert.viztool.enum.*;
import com.samskivert.viztool.viz.*;

public class Driver
{
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: Driver package_root");
            System.exit(-1);
        }
        String pkgroot = args[0];

        // run ourselves on the classpath
        String classpath = System.getProperty("java.class.path");
        ClassEnumerator enum = new ClassEnumerator(classpath);

        // print out the warnings
        ClassEnumerator.Warning[] warnings = enum.getWarnings();
        for (int i = 0; i < warnings.length; i++) {
            System.err.println("Warning: " + warnings[i].reason);
        }

        // and finally generate the visualization
        PackageEnumerator penum = new PackageEnumerator(pkgroot, enum, true);
        HierarchyVisualizer viz = new HierarchyVisualizer(pkgroot, penum);
        viz.render(10, System.out);
    }
}
