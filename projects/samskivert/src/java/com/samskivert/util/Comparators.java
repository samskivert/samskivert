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
     */
    public static final Comparator COMPARABLE = new Comparator() {
        public int compare (Object o1, Object o2)
        {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            return ((Comparable)o1).compareTo(o2);
        }
    };

    /**
     * A comparator that imposes a reverse ordering on {@link Comparable}
     * instances.
     */
    public static final Comparator REVERSE_COMPARABLE =
        new ReversingComparator(COMPARABLE);
}
