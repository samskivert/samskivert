//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
