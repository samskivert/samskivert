//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2004 Michael Bayne
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

import java.util.HashMap;

/**
 * A hashmap that maintains a count for each key.
 *
 * This implementation may change, but I find it useful to inherit all
 * the goodness of clear(), keySet(), size(), etc.
 */
public class CountHashMap extends HashMap
{
    /**
     * Increment the value associated with the specified key, return
     * the new value.
     */
    public int incrementCount (Object key, int amount)
    {
        int[] val = (int[]) get(key);
        if (val == null) {
            put(key, val = new int[1]);
        }
        val[0] += amount;
        return val[0];
    }

    /**
     * Get the count associated with the specified key.
     */
    public int getCount (Object key)
    {
        int[] val = (int[]) get(key);
        return (val == null) ? 0 : val[0];
    }
}
