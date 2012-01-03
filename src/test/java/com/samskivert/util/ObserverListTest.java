//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link ObserverList} class.
 */
public class ObserverListTest
{
    @Test
    public void runTest ()
    {
//         Log.info("Testing safe list.");
        ObserverList<TestObserver> list = ObserverList.newSafeInOrder();
        testList(list);

//         Log.info("Testing unsafe list.");
        testList(ObserverList.<TestObserver>newFastUnsafe());
    }

    public void testList (final ObserverList<TestObserver> list)
    {
        final int[] notifies = new int[1];

        for (int i = 0; i < 1000; i++) {
            // add some test observers
            list.add(new TestObserver(1));
            list.add(new TestObserver(2));
            list.add(new TestObserver(3));
            list.add(new TestObserver(4));

            int ocount = list.size();
            notifies[0] = 0;

            list.apply(new ObserverList.ObserverOp<TestObserver>() {
                public boolean apply (TestObserver obs) {
                    notifies[0]++;
                    obs.foozle();

                    // 1/3 of the time, remove the observer; 1/3 of the
                    // time append a new observer; 1/3 of the time do
                    // nothing
                    double rando = Math.random();
                    if (rando < 0.33) {
                        return false;

                    } else if (rando > 0.66) {
                        list.add(new TestObserver(5));
                    }
                    return true;
                }
            });

//             Log.info("had " + ocount + "; notified " + notifies[0] +
//                      "; size " + list.size() + ".");

            assertTrue("had " + ocount + "; notified " + notifies[0],
                       ocount == notifies[0]);
            list.clear();
        }
    }

    protected static class TestObserver
    {
        public TestObserver (int index)
        {
            _index = index;
        }

        public void foozle ()
        {
//             Log.info("foozle! " + _index);
        }

        @Override public String toString ()
        {
            return Integer.toString(_index);
        }
        protected int _index;
    }
}
