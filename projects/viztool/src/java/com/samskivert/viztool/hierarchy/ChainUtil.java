//
// $Id: ChainUtil.java,v 1.1 2001/07/04 18:24:07 mdb Exp $

package com.samskivert.viztool.viz;

import java.util.ArrayList;

import com.samskivert.viztool.Log;
import com.samskivert.viztool.enum.Enumerator;

/**
 * Chain related utility functions.
 */
public class ChainUtil
{
    /**
     * Builds a list of chains for all the classes enumerated by the
     * supplied enumerator. Classes outside the specified package root
     * will be ignored except where they are the direct parent of an
     * enumerated class inside the package root.
     *
     * @return an array list containing all of the root chains.
     */
    public static ArrayList buildChains (String pkgroot, Enumerator enum)
    {
        ArrayList roots = new ArrayList();
        computeRoots(pkgroot, enum, roots);
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
    public static void dumpClasses (ArrayList roots)
    {
        for (int i = 0; i < roots.size(); i++) {
            Chain root = (Chain)roots.get(i);
            System.out.print(root.toString());
        }
        System.out.flush();
    }

    /**
     * Scans the list of classes provided by the supplied enumerator and
     * constructs a hierarchical representation of those classes.
     */
    protected static
        void computeRoots (String pkgroot, Enumerator enum, ArrayList roots)
    {
        while (enum.hasMoreClasses()) {
            insertClass(roots, pkgroot, enum.nextClass());
        }
    }

    /**
     * Loads the supplied class and inserts it into the appropriate
     * position in the hierarchy based on its inheritance properties.
     */
    protected static
        void insertClass (ArrayList roots, String pkgroot, String clazz)
    {
        try {
            // sanity check
            if (!clazz.startsWith(pkgroot)) {
                Log.warning("Requested to process class not in target " +
                            "package [class=" + clazz +
                            ", pkgroot=" + pkgroot + "].");
                return;
            }

            // load and insert the class
            insertClass(roots, pkgroot, Class.forName(clazz));

        } catch (Exception e) {
            Log.warning("Unable to process class [class=" + clazz +
                        ", error=" + e + "].");
        }
    }

    /**
     * Inserts the specified class into the appropriate position in the
     * hierarchy based on its inheritance properties.
     */
    protected static
        void insertClass (ArrayList roots, String pkgroot, Class target)
    {
        // insert the parent of this class into the hierarchy
        Class parent = target.getSuperclass();

        // if we have no parent, we want to insert ourselves as a root
        // class
        if (parent == null || parent.equals(Object.class)) {
            insertRoot(roots, target);

        } else {
            // if our parent is not in this package, we want to insert it
            // into the hierarchy as a root class
            if (!parent.getName().startsWith(pkgroot)) {
                insertRoot(roots, parent);
            }

            // and now hang ourselves off of our parent class
            Chain chain = getChain(roots, parent);
            if (chain == null) {
                // if there's no chain for our parent class, we'll need to
                // insert it into the hierarchy
                insertClass(roots, pkgroot, parent);
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
            chain.addClass(target);
        }
    }

    protected static boolean insertRoot (ArrayList roots, Class root)
    {
        Chain chroot = new Chain(root);
        // make sure no chain already exists for this root
        if (!roots.contains(chroot)) {
            roots.add(chroot);
            return true;
        } else {
            return false;
        }
    }
}
