//
// $Id: HashIntMapTest.java,v 1.4 2002/05/23 23:29:38 ray Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

package com.samskivert.util;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.Log;

public class HashIntMapTest extends TestCase
{
    public HashIntMapTest ()
    {
        super(HashIntMapTest.class.getName());
    }

    public void runTest ()
    {
        HashIntMap table = new HashIntMap();
        populateTable(table);

        // check the table contents
        for (int i = 10; i < 20; i++) {
            Integer val = (Integer)table.get(i);
            assertTrue("get(" + i + ") == " + i, val.intValue() == i);
        }

        String keys = StringUtil.toString(table.keys());
        assertTrue("keys valid", keys.equals(TEST1));

        String elems = StringUtil.toString(table.elements());
        assertTrue("elems valid", elems.equals(TEST1));

        // remove some entries and attempt to remove some non-entries
        for (int i = 12; i < 22; i++) {
            table.remove(i);
        }

        keys = StringUtil.toString(table.keys());
        assertTrue("keys valid", keys.equals(TEST2));

        elems = StringUtil.toString(table.elements());
        assertTrue("elems valid", elems.equals(TEST2));

        // now try some serialization
        populateTable(table);

        try {
            File tmpfile = new File("/tmp/himt.dat");

            FileOutputStream fout = new FileOutputStream(tmpfile);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(table);
            out.close();

            FileInputStream fin = new FileInputStream(tmpfile);
            ObjectInputStream in = new ObjectInputStream(fin);
            HashIntMap map = (HashIntMap)in.readObject();

            // check the table contents
            for (int i = 10; i < 20; i++) {
                Integer val = (Integer)table.get(i);
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
            table.put(ii, new Integer(ii));
        }

        // now check by removing most and seeing if everything's equal
        for (int ii=1; ii < 12345; ii += 3) {
            Integer val;
            // let's keep the ones that are a multiple of 16
            if ((ii & 15) == 0) {
                val = (Integer) table.get(ii);
            } else {
                val = (Integer) table.remove(ii);
            }
            assertTrue("get(" + ii + ") == " + val, val.intValue() == ii);
        }

        // and then let's also remove the multiples of 16
        for (int ii=1; ii < 12345; ii += 3) {
            if ((ii & 15) == 0) {
                Integer val = (Integer) table.remove(ii);
                assertTrue("get(" + ii + ") == " + val, val.intValue() == ii);
            }
        }
    }

    protected void populateTable (HashIntMap table)
    {
        for (int i = 10; i < 20; i++) {
            Integer value = new Integer(i);
            table.put(i, value);
        }
    }

    public static Test suite ()
    {
        return new HashIntMapTest();
    }

    protected static final String TEST1 =
        "(19, 18, 17, 16, 15, 14, 13, 12, 11, 10)";
    protected static final String TEST2 = "(11, 10)";
}
