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

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link LRUHashMap} class.
 */
public class LRUHashMapTest
{
    @Test
    public void runTest ()
    {
        LRUHashMap<String,Integer> map =
            new LRUHashMap<String,Integer>(10, new LRUHashMap.ItemSizer<Integer>() {
            public int computeSize (Integer item) {
                return item.intValue();
            }
        });

        map.put("one.1", 1);
        assertTrue("size == 1", map.size() == 1);
        map.put("one.2", 1);
        assertTrue("size == 2", map.size() == 2);
        map.put("one.3", 1);
        assertTrue("size == 3", map.size() == 3);
        map.put("one.4", 1);
        assertTrue("size == 4", map.size() == 4);
        map.put("one.5", 1);
        assertTrue("size == 5", map.size() == 5);
        map.put("three.1", 3);
        assertTrue("size == 6", map.size() == 6);
        map.put("five.1", 5);
        assertTrue("size == 4", map.size() == 4);
        map.put("three.2", 3);
        assertTrue("size == 2", map.size() == 2);
        map.put("three.3", 3);
        assertTrue("size == 2", map.size() == 2);
    }
}
