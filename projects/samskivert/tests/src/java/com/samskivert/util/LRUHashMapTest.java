//
// $Id: LRUHashMapTest.java,v 1.1 2003/01/17 00:40:45 mdb Exp $

package com.samskivert.util;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link LRUHashMap} class.
 */
public class LRUHashMapTest extends TestCase
{
    public LRUHashMapTest ()
    {
        super(LRUHashMapTest.class.getName());
    }

    public void runTest ()
    {
        LRUHashMap map = new LRUHashMap(10, new LRUHashMap.ItemSizer() {
            public int computeSize (Object item) {
                return ((Integer)item).intValue();
            }
        });

        map.put("one.1", new Integer(1));
        assertTrue("size == 1", map.size() == 1);
        map.put("one.2", new Integer(1));
        assertTrue("size == 2", map.size() == 2);
        map.put("one.3", new Integer(1));
        assertTrue("size == 3", map.size() == 3);
        map.put("one.4", new Integer(1));
        assertTrue("size == 4", map.size() == 4);
        map.put("one.5", new Integer(1));
        assertTrue("size == 5", map.size() == 5);
        map.put("three.1", new Integer(3));
        assertTrue("size == 6", map.size() == 6);
        map.put("five.1", new Integer(5));
        assertTrue("size == 4", map.size() == 4);
        map.put("three.2", new Integer(3));
        assertTrue("size == 2", map.size() == 2);
        map.put("three.3", new Integer(3));
        assertTrue("size == 2", map.size() == 2);
    }

    public static Test suite ()
    {
        return new LRUHashMapTest();
    }

    public static void main (String[] args)
    {
        LRUHashMapTest test = new LRUHashMapTest();
        test.runTest();
    }
}
