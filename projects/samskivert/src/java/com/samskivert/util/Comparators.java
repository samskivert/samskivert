//
// $Id: Comparators.java,v 1.1 2001/07/13 23:41:06 mdb Exp $

package com.samskivert.util;

import java.util.Comparator;

/**
 * A repository for standard comparators.
 */
public class Comparators
{
    public static final Comparator STRING = new Comparator()
    {
        public int compare (Object o1, Object o2)
        {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            return ((String)o1).compareTo((String)o2);
        }

        public boolean equals (Object other)
        {
            return (other == this);
        }
    };
}
