//
// $Id: PackageEnumerator.java,v 1.1 2001/07/04 18:22:26 mdb Exp $

package com.samskivert.viztool.enum;

/**
 * The package enumerator filters out only classes from the specified
 * package (and subpackages) from the class enumerator provided at
 * construct time.
 */
public class PackageEnumerator extends FilterEnumerator
{
    public PackageEnumerator (String pkg, ClassEnumerator source)
    {
        super(source);
        _package = pkg;
    }

    protected boolean filterClass (String clazz)
    {
        return !clazz.startsWith(_package);
    }

    public static void main (String[] args)
    {
        // run ourselves on the classpath
        String classpath = System.getProperty("java.class.path");
        ClassEnumerator enum = new ClassEnumerator(classpath);
        String pkg = "com.samskivert.viztool.enum";
        PackageEnumerator penum = new PackageEnumerator(pkg, enum);

        // print out the warnings
        ClassEnumerator.Warning[] warnings = enum.getWarnings();
        for (int i = 0; i < warnings.length; i++) {
            System.out.println("Warning: " + warnings[i].reason);
        }

        // enumerate over whatever classes match our package
        while (penum.hasMoreClasses()) {
            System.out.println("Class: " + penum.nextClass());
        }
    }

    protected String _package;
}
