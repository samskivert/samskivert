//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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
