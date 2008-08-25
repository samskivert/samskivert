//
// $Id: CheapIntMapTest.java,v 1.1 2003/02/06 19:57:29 mdb Exp $

package com.samskivert.util.tests;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.CheapIntMap;

/**
 * Tests the {@link CheapIntMap} class.
 */
public class CheapIntMapTest extends TestCase
{
    public CheapIntMapTest ()
    {
        super(CheapIntMapTest.class.getName());
    }

    public void runTest ()
    {
        CheapIntMap map = new CheapIntMap(10);

        for (int ii = 0; ii < 100; ii += 20) {
            map.put(ii, new Integer(ii));
        }

        for (int ii = 0; ii < 5; ii++) {
            map.put(ii, new Integer(ii));
        }

        for (int ii = 0; ii < 100; ii++) {
            Object val = map.get(ii);
            if (val != null) {
                System.out.println(ii + " => " + val);
            }
        }

        for (int ii = 0; ii < 100; ii += 20) {
            System.out.println("Removing " + map.remove(ii));
        }

        for (int ii = 10; ii > 0; ii--) {
            map.put(ii, new Integer(ii));
        }

        for (int ii = 0; ii < 100; ii++) {
            Object val = map.get(ii);
            if (val != null) {
                System.out.println(ii + " => " + val);
            }
        }
    }

    public static Test suite ()
    {
        return new CheapIntMapTest();
    }

    public static void main (String[] args)
    {
        CheapIntMapTest test = new CheapIntMapTest();
        test.runTest();
    }
}
