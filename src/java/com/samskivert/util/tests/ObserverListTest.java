//
// $Id: ObserverListTest.java,v 1.2 2004/02/25 13:21:08 mdb Exp $

package com.samskivert.util.tests;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.ObserverList;

/**
 * Tests the {@link ObserverList} class.
 */
public class ObserverListTest extends TestCase
{
    public ObserverListTest ()
    {
        super(ObserverListTest.class.getName());
    }

    public void runTest ()
    {
//         Log.info("Testing safe list.");
        testList(new ObserverList<TestObserver>(ObserverList.SAFE_IN_ORDER_NOTIFY));

//         Log.info("Testing unsafe list.");
        testList(new ObserverList<TestObserver>(ObserverList.FAST_UNSAFE_NOTIFY));
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

    public static Test suite ()
    {
        return new ObserverListTest();
    }

    public static void main (String[] args)
    {
        ObserverListTest test = new ObserverListTest();
        test.runTest();
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

        public String toString ()
        {
            return Integer.toString(_index);
        }
        protected int _index;
    }
}
