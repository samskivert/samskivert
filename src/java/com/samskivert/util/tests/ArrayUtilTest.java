//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util.tests;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.StringUtil;

import static com.samskivert.Log.log;

/**
 * Tests the {@link ArrayUtil} class.
 */
public class ArrayUtilTest extends TestCase
{
    public ArrayUtilTest ()
    {
        super(ArrayUtilTest.class.getName());
    }

    @Override
    public void runTest ()
    {
        // test reversing an array
        int[] values = new int[] { 0 };
        int[] work = values.clone();
        ArrayUtil.reverse(work);
        log.info("reverse: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2 };
        work = values.clone();
        ArrayUtil.reverse(work);
        log.info("reverse: " + StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.reverse(work, 0, 2);
        log.info("reverse first-half: " + StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.reverse(work, 1, 2);
        log.info("reverse second-half: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3, 4 };
        work = values.clone();
        ArrayUtil.reverse(work, 1, 3);
        log.info("reverse middle: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3 };
        work = values.clone();
        ArrayUtil.reverse(work);
        log.info("reverse even: " + StringUtil.toString(work));

        // test shuffling two elements
        values = new int[] { 0, 1 };
        work = values.clone();
        ArrayUtil.shuffle(work, 0, 1);
        log.info("first-half shuffle: " + StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.shuffle(work, 1, 1);
        log.info("second-half shuffle: " + StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.shuffle(work);
        log.info("full shuffle: " + StringUtil.toString(work));

        // test shuffling three elements
        values = new int[] { 0, 1, 2 };
        work = values.clone();
        ArrayUtil.shuffle(work, 0, 2);
        log.info("first-half shuffle: " + StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.shuffle(work, 1, 2);
        log.info("second-half shuffle: " + StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.shuffle(work);
        log.info("full shuffle: " + StringUtil.toString(work));

        // test shuffling ten elements
        values = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        work = values.clone();
        ArrayUtil.shuffle(work, 0, 5);
        log.info("first-half shuffle: " + StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.shuffle(work, 5, 5);
        log.info("second-half shuffle: " + StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.shuffle(work);
        log.info("full shuffle: " + StringUtil.toString(work));

        // test splicing with simple truncate beyond offset
        values = new int[] { 0, 1, 2 };
        work = values.clone();
        work = ArrayUtil.splice(work, 0);
        log.info("splice truncate 0: " + StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 1);
        log.info("splice truncate 1: " + StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 2);
        log.info("splice truncate 2: " + StringUtil.toString(work));

        values = new int[] { 0 };
        work = values.clone();
        work = ArrayUtil.splice(work, 0);
        log.info("single element splice truncate 0: " +
                 StringUtil.toString(work));

        // test splicing out a single element
        values = new int[] { 0, 1, 2 };
        work = values.clone();
        work = ArrayUtil.splice(work, 0, 1);
        log.info("splice concat 0, 1: " + StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 1, 1);
        log.info("splice concat 1, 1: " + StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 2, 1);
        log.info("splice concat 2, 1: " + StringUtil.toString(work));

        // test splicing out two elements
        values = new int[] { 0, 1, 2, 3 };
        work = values.clone();
        work = ArrayUtil.splice(work, 0, 2);
        log.info("splice concat 0, 2: " + StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 1, 2);
        log.info("splice concat 1, 2: " + StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 2, 2);
        log.info("splice concat 2, 2: " + StringUtil.toString(work));
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
