//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import com.samskivert.annotation.ReplacedBy;

/**
 * Provides static methods for creating mutable {@code IntMap} instances easily.
 * You can replace code like:
 *
 * <p>{@code IntMap<String> map = new HashIntMap<String>();}
 *
 * <p>with just:
 *
 * <p>{@code IntMap<String> map = IntMaps.newHashIntMap();}
 */
@ReplacedBy(value="com.google.common.collect.Maps",
            reason="Boxing shouldn't be a major concern. It's probably better to stick to " +
            "standard classes rather than worry about a tiny memory or performance gain.")
public class IntMaps
{
    /**
     * Creates a {@code HashIntMap} instance.
     *
     * @return a newly-created, initially-empty {@code HashIntMap}
     */
    @ReplacedBy(value="com.google.common.collect.Maps#newHashMap()")
    public static <V> HashIntMap<V> newHashIntMap()
    {
        return new HashIntMap<V>();
    }
}
