//
// $Id: HashIntMapTest.java,v 1.1 2001/11/26 19:34:31 mdb Exp $
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
import com.samskivert.test.TestUtil;

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
            assert("get(" + i + ") == " + i, val.intValue() == i);
        }

        String keys = StringUtil.toString(table.keys());
        assert("keys valid", keys.equals(TEST1));

        String elems = StringUtil.toString(table.elements());
        assert("elems valid", elems.equals(TEST1));

        // remove some entries and attempt to remove some non-entries
        for (int i = 12; i < 22; i++) {
            table.remove(i);
        }

        keys = StringUtil.toString(table.keys());
        assert("keys valid", keys.equals(TEST2));

        elems = StringUtil.toString(table.elements());
        assert("elems valid", elems.equals(TEST2));

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
                assert("get(" + i + ") == " + i, val.intValue() == i);
            }

            tmpfile.delete();

        } catch (Exception e) {
            e.printStackTrace();
            fail("serialization failure");
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
