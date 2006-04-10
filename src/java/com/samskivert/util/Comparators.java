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
     * A comparator that compares the toString() value of all objects
     * case insensitively.
     */
    public static final Comparator<Object> LEXICAL_CASE_INSENSITIVE =
        new Comparator<Object>() {
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
     * A comparator that compares {@link Comparable} instances.
     */
    public static final Comparator<Object> COMPARABLE =
        new Comparator<Object>() {
        @SuppressWarnings("unchecked")
        public int compare (Object o1, Object o2)
        {
            if (o1 == o2) { // catches null == null
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }
            return ((Comparable<Object>)o1).compareTo(o2); // null-free
        }
    };
}
