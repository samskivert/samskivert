//
// $Id: Comparators.java,v 1.3 2002/02/19 03:38:06 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

import java.util.Comparator;

/**
 * A repository for standard comparators.
 */
public class Comparators
{
    /**
     * A comparator that can be used to reverse the results of another
     * comparator.
     * TODO: deprecate this when we more globally move to 1.5:
     * @use java.util.Collections.reverseOrder(Comparator c);
     */
    public static class ReversingComparator implements Comparator
    {
        public ReversingComparator (Comparator reversable)
        {
            _reversable = reversable;
        }

        // documentation inherited from interface Comparator
        public int compare (Object o1, Object o2)
        {
            return _reversable.compare(o2, o1); // switching the order
        }

        protected Comparator _reversable;
    }

    /**
     * A comparator that compares {@link Comparable} instances.
     * Can you believe this isn't defined somewhere in the standard
     * java libraries?
     */
    public static final Comparator COMPARABLE = new Comparator() {
        public int compare (Object o1, Object o2)
        {
            if (o1 == o2) { // catches null == null
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }
            return ((Comparable)o1).compareTo(o2); // null-free
        }
    };

    /**
     * A comparator that compares the toString() value of all objects
     * case insensitively.
     */
    public static final Comparator LEXICAL_CASE_INSENSITIVE = new Comparator() {
        public int compare (Object o1, Object o2)
        {
            if (o1 == o2) { // catches null == null
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }
            // now that we've filtered all nulls, compare the toString()s
            return String.CASE_INSENSITIVE_ORDER.compare(
                o1.toString(), o2.toString());
        }
    };

    /**
     * A comparator that imposes a reverse ordering on {@link Comparable}
     * instances.
     *
     * @deprecated use java.util.Collections.reverseOrder()
     */
    public static final Comparator REVERSE_COMPARABLE =
        java.util.Collections.reverseOrder();
}
