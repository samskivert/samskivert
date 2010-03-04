// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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
 * A utility class to succinctly construct a HashMap seeded with particular mappings.
 */
public class Mapping
{
    public static <K, V> HashMap<K, V> of (K k1, V v1)
    {
        return of(new Object[] { k1, v1 });
    }

    public static <K, V> HashMap<K, V> of (K k1, V v1, K k2, V v2)
    {
        return of(new Object[] { k1, v1, k2, v2 });
    }

    public static <K, V> HashMap<K, V> of (K k1, V v1, K k2, V v2, K k3, V v3)
    {
        return of(new Object[] { k1, v1, k2, v2, k3, v3 });
    }

    public static <K, V> HashMap<K, V> of (K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4)
    {
        return of(new Object[] { k1, v1, k2, v2, k3, v3, k4, v4 });
    }

    public static <K, V> HashMap<K, V> of (
        K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5)
    {
        return of(new Object[] { k1, v1, k2, v2, k3, v3, k4, v4, k5, v5 });
    }

    public static <K, V> HashMap<K, V> of (
        K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6)
    {
        return of(new Object[] { k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6 });
    }

    public static <K, V> HashMap<K, V> of (
        K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7)
    {
        return of(new Object[] { k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7 });
    }

    protected static <K, V> HashMap<K, V> of (Object... pairs)
    {
        HashMap<K, V> map = new HashMap<K, V>();
        for (int ii = 0; ii < pairs.length; ii += 2) {
            @SuppressWarnings("unchecked")
            K k = (K)pairs[ii];
            @SuppressWarnings("unchecked")
            V v = (V)pairs[ii + 1];
            map.put(k, v);
        }
        return map;
    }

    private Mapping ()
    {
    }
}
