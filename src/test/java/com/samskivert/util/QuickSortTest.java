//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Comparator;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link QuickSort} class.
 */
public class QuickSortTest
{
    @Test
    public void runTest ()
    {
        Integer[] a = new Integer[100];

        Comparator<Integer> comp = new Comparator<Integer>() {
            public int compare (Integer x, Integer y) {
                return x.intValue() - y.intValue();
            }
        };

        for (int d = 1; d <= 100; d++) {
            for (int n = 0; n < 100; n++) {
                a[n] = (n / d);
                QuickSort.sort(a, 0, n, comp);
                for (int i = 0; i <= n; i++) {
                    assertTrue("Failure for up " + n + "/" + d,
                               a[i].intValue() == i / d);
                }
            }
        }
        // System.out.println("up test ok");

        for (int d = 1; d <= 100; d++) {
            for (int n = 0; n < 100; n++) {
                for (int i = 0; i <= n; i++) {
                    a[i] = ((n - i) / d);
                }
                QuickSort.sort(a, 0, n, comp);
                for (int i = 0; i <= n; i++) {
                    assertTrue("Failure for down " + n + "/" + d,
                               a[i].intValue() == i / d);
                }
            }
        }
        // System.out.println("down test ok");

        int tests = 1000;
        for (int sorts = 0; sorts < tests; sorts++) {
            int n = rand(100);
            for (int i = 0; i <= n; i++) {
                a[i] = rand(30000);
            }

            QuickSort.sort(a, 0, n, comp);
            for (int i = 0; i < n; i++) {
                assertTrue("Failure for random " + n,
                           a[i].intValue() <= a[i+1].intValue());
            }

            QuickSort.sort(a, 0, n, comp);
            for (int i = 0; i < n; i++) {
                assertTrue("Failure for random " + n + " (resort)",
                           a[i].intValue() <= a[i+1].intValue());
            }

            a[rand(n+1)] = rand(30000);
            QuickSort.sort(a, 0, n, comp);
            for (int i = 0; i < n; i++) {
                assertTrue("Failure for random " + n + " (resort 2)",
                           a[i].intValue() <= a[i+1].intValue());
            }
        }
        // System.out.println("successfully sorted " + tests +
        // " random arrays");
    }

    private static int rand (int n)
    {
        return (int)(Math.random() * n);
    }
}
