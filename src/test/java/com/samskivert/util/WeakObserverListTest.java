//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link WeakObserverList} class.
 */
public class WeakObserverListTest
{
    @Test public void testPruneRemovesCollectedObservers ()
    {
        WeakObserverList<Object> list = WeakObserverList.newSafeInOrder();
        List<Object> live = new ArrayList<Object>();
        for (int ii = 0; ii < 100; ii++) {
            Object obs = new Object();
            list.add(obs);
            if (ii % 10 == 0) {
                live.add(obs);
            }
        }
        forceGc();
        list.prune();
        assertEquals(live.size(), list.size());
        assertEquals(live.size(), countNotified(list));
    }

    @Test public void testAddCompactsCollectedObservers ()
    {
        checkAddCompacts(WeakObserverList.<Object>newSafeInOrder());
        checkAddCompacts(WeakObserverList.<Object>newFastUnsafe());
    }

    protected void checkAddCompacts (WeakObserverList<Object> list)
    {
        List<Object> permanents = new ArrayList<Object>();
        for (int ii = 0; ii < 5; ii++) {
            Object obs = new Object();
            permanents.add(obs);
            list.add(obs);
        }

        // churn many observers whose only reference is the list's weak one; without
        // add-triggered pruning the list would grow to the total ever added
        for (int batch = 0; batch < 30; batch++) {
            for (int ii = 0; ii < 20; ii++) {
                list.add(new Object());
            }
            forceGc();
            assertTrue("list failed to compact: size=" + list.size(), list.size() <= 100);
        }

        forceGc();
        list.prune();
        assertEquals(permanents.size(), list.size());
        assertEquals(permanents.size(), countNotified(list));
    }

    /** Applies a no-op to the list, returning the number of observers notified. */
    protected static int countNotified (WeakObserverList<Object> list)
    {
        final int[] count = new int[1];
        list.apply(new ObserverList.ObserverOp<Object>() {
            public boolean apply (Object obs) {
                count[0]++;
                return true;
            }
        });
        return count[0];
    }

    /** Encourages the JVM to clear weak references, skipping the test if it declines. */
    protected static void forceGc ()
    {
        WeakReference<Object> canary = new WeakReference<Object>(new Object());
        for (int ii = 0; ii < 10 && canary.get() != null; ii++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Assume.assumeTrue("JVM declined to clear weak references", canary.get() == null);
    }
}
