//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.*;
import static org.junit.Assert.*;

public class HashIntMapTest
{
    @Test
    public void runTest ()
    {
        HashIntMap<Integer> table = new HashIntMap<Integer>();
        populateTable(table);

        // check the table contents
        for (int i = 10; i < 20; i++) {
            Integer val = table.get(i);
            assertTrue("get(" + i + ") == " + i, val.intValue() == i);
        }

        checkContents(table, TEST1, TEST1);

        // remove some entries and attempt to remove some non-entries
        for (int i = 12; i < 22; i++) {
            table.remove(i);
        }

        checkContents(table, TEST2, TEST2);

        // now try some serialization
        populateTable(table);

        try {
            File tmpfile = File.createTempFile("himt", "dat");

            FileOutputStream fout = new FileOutputStream(tmpfile);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(table);
            out.close();

            FileInputStream fin = new FileInputStream(tmpfile);
            ObjectInputStream in = new ObjectInputStream(fin);
            @SuppressWarnings("unchecked") HashIntMap<Integer> map =
                (HashIntMap<Integer>)in.readObject();

            // check the table contents
            for (int i = 10; i < 20; i++) {
                Integer val = map.get(i);
                assertTrue("get(" + i + ") == " + i, val.intValue() == i);
            }

            tmpfile.delete();

        } catch (Exception e) {
            e.printStackTrace();
            fail("serialization failure");
        }

        table.clear();
        // now try putting lots and lots of values in the table
        // so that it grows a bunch
        for (int ii=1; ii < 12345; ii += 3) {
            table.put(ii, Integer.valueOf(ii));
        }

        // now check by removing most and seeing if everything's equal
        for (int ii=1; ii < 12345; ii += 3) {
            Integer val;
            // let's keep the ones that are a multiple of 16
            if ((ii & 15) == 0) {
                val = table.get(ii);
            } else {
                val = table.remove(ii);
            }
            assertTrue("get(" + ii + ") == " + val, val.intValue() == ii);
        }

        // and then let's also remove the multiples of 16
        for (int ii = 1; ii < 12345; ii += 3) {
            if ((ii & 15) == 0) {
                Integer val = table.remove(ii);
                assertTrue("get(" + ii + ") == " + val, val.intValue() == ii);
            }
        }
    }

    protected void populateTable (HashIntMap<Integer> table)
    {
        for (int ii = 10; ii < 20; ii++) {
            table.put(ii, Integer.valueOf(ii));
        }
    }

    protected void checkContents (HashIntMap<Integer> table, String exkeys, String exvals)
    {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        keys.addAll(table.keySet());
        Collections.sort(keys);
        String keystr = StringUtil.toString(keys);
        assertTrue(keystr + ".equals(" + exkeys + ")", keystr.equals(exkeys));

        ArrayList<Integer> values = new ArrayList<Integer>();
        values.addAll(table.values());
        Collections.sort(values);
        String valuestr = StringUtil.toString(values);
        assertTrue(valuestr + ".equals(" + exvals + ")", valuestr.equals(exvals));
    }

    protected static final String TEST1 = "(10, 11, 12, 13, 14, 15, 16, 17, 18, 19)";
    protected static final String TEST2 = "(10, 11)";
}
