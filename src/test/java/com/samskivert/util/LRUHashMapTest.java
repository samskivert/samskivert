//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
