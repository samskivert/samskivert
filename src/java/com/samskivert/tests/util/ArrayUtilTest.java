//
// $Id: ArrayUtilTest.java,v 1.2 2002/09/06 02:12:26 shaper Exp $

package com.samskivert.util.tests;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.Log;
import com.samskivert.util.ArrayUtil;
import com.samskivert.util.StringUtil;

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
        ArrayUtil.reverse(work);
        Log.info("reverse: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2 };
        work = (int[])values.clone();
        ArrayUtil.reverse(work);
        Log.info("reverse: " + StringUtil.toString(work));

        work = (int[])values.clone();
        ArrayUtil.reverse(work, 0, 2);
        Log.info("reverse first-half: " + StringUtil.toString(work));

        work = (int[])values.clone();
        ArrayUtil.reverse(work, 1, 2);
        Log.info("reverse second-half: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3, 4 };
        work = (int[])values.clone();
        ArrayUtil.reverse(work, 1, 3);
        Log.info("reverse middle: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3 };
        work = (int[])values.clone();
        ArrayUtil.reverse(work);
        Log.info("reverse even: " + StringUtil.toString(work));

        // test shuffling two elements
        values = new int[] { 0, 1 };
        work = (int[])values.clone();
        ArrayUtil.shuffle(work, 0, 1);
        Log.info("first-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        ArrayUtil.shuffle(work, 1, 1);
        Log.info("second-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        ArrayUtil.shuffle(work);
        Log.info("full shuffle: " + StringUtil.toString(work));

        // test shuffling three elements
        values = new int[] { 0, 1, 2 };
        work = (int[])values.clone();
        ArrayUtil.shuffle(work, 0, 2);
        Log.info("first-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        ArrayUtil.shuffle(work, 1, 2);
        Log.info("second-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        ArrayUtil.shuffle(work);
        Log.info("full shuffle: " + StringUtil.toString(work));

        // test shuffling ten elements
        values = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        work = (int[])values.clone();
        ArrayUtil.shuffle(work, 0, 5);
        Log.info("first-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        ArrayUtil.shuffle(work, 5, 5);
        Log.info("second-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        ArrayUtil.shuffle(work);
        Log.info("full shuffle: " + StringUtil.toString(work));

        // test splicing with simple truncate beyond offset
        values = new int[] { 0, 1, 2 };
        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 0);
        Log.info("splice truncate 0: " + StringUtil.toString(work));

        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 1);
        Log.info("splice truncate 1: " + StringUtil.toString(work));

        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 2);
        Log.info("splice truncate 2: " + StringUtil.toString(work));

        values = new int[] { 0 };
        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 0);
        Log.info("single element splice truncate 0: " +
                 StringUtil.toString(work));

        // test splicing out a single element
        values = new int[] { 0, 1, 2 };
        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 0, 1);
        Log.info("splice concat 0, 1: " + StringUtil.toString(work));

        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 1, 1);
        Log.info("splice concat 1, 1: " + StringUtil.toString(work));

        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 2, 1);
        Log.info("splice concat 2, 1: " + StringUtil.toString(work));

        // test splicing out two elements
        values = new int[] { 0, 1, 2, 3 };
        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 0, 2);
        Log.info("splice concat 0, 2: " + StringUtil.toString(work));

        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 1, 2);
        Log.info("splice concat 1, 2: " + StringUtil.toString(work));

        work = (int[])values.clone();
        work = ArrayUtil.splice(work, 2, 2);
        Log.info("splice concat 2, 2: " + StringUtil.toString(work));
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
