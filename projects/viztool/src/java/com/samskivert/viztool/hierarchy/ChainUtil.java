//
// $Id: ChainUtil.java,v 1.8 2001/12/03 08:34:53 mdb Exp $
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

package com.samskivert.viztool.hierarchy;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.viztool.Log;

/**
 * Chain related utility functions.
 */
public class ChainUtil
{
    /**
     * Builds a list of chains for the classes enumerated by the supplied
     * enumerator. Classes outside the specified package root will be
     * ignored except where they are the direct parent of an enumerated
     * class inside the package root.
     *
     * @return an array list containing all of the root chains.
     */
    public static ArrayList buildChains (
        String pkgroot, String pkg, Iterator iter)
    {
        ArrayList roots = new ArrayList();
        computeRoots(pkgroot, pkg, iter, roots);
        return roots;
    }

    /**
     * Looks up the chain that contains the specified target class as it's
     * root class in the supplied array list of chains.
     *
     * @return the matching chain or null if no chain could be found.
     */
    public static Chain getChain (ArrayList roots, Class target)
    {
        // figure out which of our root chains (if any) contains the
        // specified class
        for (int i = 0; i < roots.size(); i++) {
            Chain root = (Chain)roots.get(i);
            Chain chain = root.getChain(target);
            if (chain != null) {
                return chain;
            }
        }

        return null;
    }

    /**
     * Dumps the classes in the supplied array list of chain instances to
     * stdout.
     */
    public static void dumpClasses (PrintStream out, ArrayList roots)
    {
        for (int i = 0; i < roots.size(); i++) {
            Chain root = (Chain)roots.get(i);
            out.print(root.toString());
        }
        out.flush();
    }

    /**
     * Scans the list of classes provided by the supplied iterator and
     * constructs a hierarchical representation of those classes.
     */
    protected static void computeRoots (
        String pkgroot, String pkg, Iterator iter, ArrayList roots)
    {
        while (iter.hasNext()) {
            Class clazz = (Class)iter.next();
            String name = clazz.getName();
            // skip classes not in the package in question
            if (!name.startsWith(pkg) ||
                name.substring(pkg.length()+1).indexOf(".") != -1) {
                continue;
            }
            insertClass(roots, pkgroot, clazz, false);
        }
    }

    /**
     * Inserts the specified class into the appropriate position in the
     * hierarchy based on its inheritance properties.
     */
    protected static void insertClass (
        ArrayList roots, String pkgroot, Class target, boolean outpkg)
    {
        // insert the parent of this class into the hierarchy
        Class parent = target.getSuperclass();
        String name = generateName(target, pkgroot, outpkg);

        // if we have no parent, we want to insert ourselves as a root
        // class
        if (parent == null || parent.equals(Object.class)) {
            insertRoot(roots, name, target, true);

        } else {
            String tpkg = pkgFromClass(target.getName());
            String ppkg = pkgFromClass(parent.getName());

            // if our parent is not in this package, we want to insert it
            // into the hierarchy as a root class
            if (!tpkg.equals(ppkg)) {
                String pname = generateName(parent, pkgroot, true);
                insertRoot(roots, pname, parent, false);
            }

            // and now hang ourselves off of our parent class
            Chain chain = getChain(roots, parent);
            if (chain == null) {
                // if there's no chain for our parent class, we'll need to
                // insert it into the hierarchy
                boolean samepkg = pkgFromClass(parent.getName()).equals(
                    pkgFromClass(target.getName()));
                insertClass(roots, pkgroot, parent, !samepkg);
                // and refetch our chain
                chain = getChain(roots, parent);
                // sanity check
                if (chain == null) {
                    Log.warning("Chain still doesn't exist even though " +
                                "we inserted our parent " +
                                "[class=" + target.getName() +
                                ", parent=" + parent.getName() + "].");
                    return;
                }
            }

            // add class will ignore our request if this class was already
            // added due to some previous operation
            chain.addClass(name, target);
        }
    }

    protected static String generateName (Class target, String pkgroot,
                                          boolean outpkg)
    {
        String name;

        if (outpkg) {
            // start with the fully qualified class name
            name = target.getName();
            // if we're in the package root, we want to strip off the
            // package root and prefix ...
            if (name.startsWith(pkgroot)) {
                name = ".." + name.substring(pkgroot.length());
            }

        } else {
            name = nameFromClass(target.getName());
        }

        return name;
    }

    /**
     * Returns just the package qualifier given a fully qualified class
     * name.
     */
    public static String pkgFromClass (String fqn)
    {
        int didx = fqn.lastIndexOf(".");
        return (didx == -1) ? fqn : fqn.substring(0, didx);
    }

    /**
     * Returns the unqualified class name given a fully qualified class
     * name.
     */
    public static String nameFromClass (String fqn)
    {
        int didx = fqn.lastIndexOf(".");
        return (didx == -1) ? fqn : fqn.substring(didx+1);
    }

    protected static boolean insertRoot (ArrayList roots, String name,
                                         Class root, boolean inpkg)
    {
        Chain chroot = new Chain(name, root, inpkg);
        // make sure no chain already exists for this root
        if (!roots.contains(chroot)) {
            roots.add(chroot);
            return true;
        } else {
            return false;
        }
    }
}
