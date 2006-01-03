//
// $Id: QuickSortTest.java,v 1.2 2002/04/11 04:07:42 mdb Exp $

package com.samskivert.util;

import java.util.Comparator;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link QuickSort} class.
 */
public class QuickSortTest extends TestCase
{
    public QuickSortTest ()
    {
        super(QuickSortTest.class.getName());
    }

    public void runTest ()
    {
        Integer[] a = new Integer[100];

        Comparator comp = new Comparator() {
            public int compare (Object x, Object y) {
                return ((Integer)x).intValue() - ((Integer)y).intValue();
            }
        };

        for (int d = 1; d <= 100; d++) {
            for (int n = 0; n < 100; n++) {
                a[n] = new Integer(n / d);
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
                    a[i] = new Integer((n - i) / d);
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
                a[i] = new Integer(rand(30000));
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

            a[rand(n+1)] = new Integer(rand(30000));
            QuickSort.sort(a, 0, n, comp);
            for (int i = 0; i < n; i++) {
                assertTrue("Failure for random " + n + " (resort 2)",
                           a[i].intValue() <= a[i+1].intValue());
            }
        }
        // System.out.println("successfully sorted " + tests +
        // " random arrays");
    }

    public static Test suite ()
    {
        return new QuickSortTest();
    }

    public static void main (String[] args)
    {
        QuickSortTest test = new QuickSortTest();
        test.runTest();
    }

    private static int rand (int n)
    {
        return (int)(Math.random() * n);
    }
}
