//
// $Id: ArrayUtilTest.java,v 1.1 2002/08/12 01:10:32 mdb Exp $

package com.samskivert.util;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link ArrayUtil} class.
 */
public class ArrayUtilTest extends TestCase
{
    public ArrayUtilTest ()
    {
        super(ArrayUtilTest.class.getName());
    }

    public void runTest ()
    {
        // test reversing an array
        int[] values = new int[] { 0 };
        int[] work = (int[])values.clone();
        reverse(work);
        Log.info("reverse: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2 };
        work = (int[])values.clone();
        reverse(work);
        Log.info("reverse: " + StringUtil.toString(work));

        work = (int[])values.clone();
        reverse(work, 0, 2);
        Log.info("reverse first-half: " + StringUtil.toString(work));

        work = (int[])values.clone();
        reverse(work, 1, 2);
        Log.info("reverse second-half: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3, 4 };
        work = (int[])values.clone();
        reverse(work, 1, 3);
        Log.info("reverse middle: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3 };
        work = (int[])values.clone();
        reverse(work);
        Log.info("reverse even: " + StringUtil.toString(work));

        // test shuffling two elements
        values = new int[] { 0, 1 };
        work = (int[])values.clone();
        shuffle(work, 0, 1);
        Log.info("first-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work, 1, 1);
        Log.info("second-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work);
        Log.info("full shuffle: " + StringUtil.toString(work));

        // test shuffling three elements
        values = new int[] { 0, 1, 2 };
        work = (int[])values.clone();
        shuffle(work, 0, 2);
        Log.info("first-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work, 1, 2);
        Log.info("second-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work);
        Log.info("full shuffle: " + StringUtil.toString(work));

        // test shuffling ten elements
        values = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        work = (int[])values.clone();
        shuffle(work, 0, 5);
        Log.info("first-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work, 5, 5);
        Log.info("second-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work);
        Log.info("full shuffle: " + StringUtil.toString(work));
    }

    public static Test suite ()
    {
        return new ArrayUtilTest();
    }

    public static void main (String[] args)
    {
        ArrayUtilTest test = new ArrayUtilTest();
        test.runTest();
    }
}
