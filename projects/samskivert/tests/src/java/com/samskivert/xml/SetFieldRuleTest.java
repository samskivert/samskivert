//
// $Id: SetFieldRuleTest.java,v 1.2 2001/12/13 01:31:23 mdb Exp $
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

package com.samskivert.xml;

import java.io.InputStream;
import java.io.FileInputStream;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.digester.Digester;

import com.samskivert.Log;
import com.samskivert.test.TestUtil;
import com.samskivert.util.StringUtil;

public class SetFieldRuleTest extends TestCase
{
    public SetFieldRuleTest ()
    {
        super(SetFieldRuleTest.class.getName());
    }

    public static class TestObject
    {
        public int intField;
        public String stringField;
        public Integer integerField;
        public int[] intArrayField;
        public String[] stringArrayField;

        public String toString ()
        {
            return "[intField=" + intField + ", stringField=" + stringField +
                ", integerField=" + integerField +
                ", intArrayField=" + StringUtil.toString(intArrayField) +
                ", stringArrayField=" + StringUtil.toString(stringArrayField) +
                "]";
        }
    }

    public void runTest ()
    {
        Digester digester = new Digester();

        // create our object and push it onto the digester
        TestObject object = new TestObject();
        digester.push(object);

        // set up some rules
        digester.addRule("object/intField",
                         new SetFieldRule(digester, "intField"));
        digester.addRule("object/stringField",
                         new SetFieldRule(digester, "stringField"));
        digester.addRule("object/integerField",
                         new SetFieldRule(digester, "integerField"));
        digester.addRule("object/intArrayField",
                         new SetFieldRule(digester, "intArrayField"));
        digester.addRule("object/stringArrayField",
                         new SetFieldRule(digester, "stringArrayField"));

        try {
            String xmlpath =
                TestUtil.getResourcePath("rsrc/xml/setfieldtest.xml");
            InputStream input = new FileInputStream(xmlpath);
            digester.parse(input);
            input.close();
        } catch (Exception e) {
            fail("Parsing failed: " + e);
        }

        assert(EXPECTED.equals(object.toString()));
    }

    public static Test suite ()
    {
        return new SetFieldRuleTest();
    }

    public static void main (String[] args)
    {
        SetFieldRuleTest test = new SetFieldRuleTest();
        test.runTest();
    }

    protected static final String EXPECTED =
        "[intField=5, stringField=howdy partner!, integerField=15, " +
        "intArrayField=(1, 2, 3, 4, 5), " +
        "stringArrayField=(one, two, three, four, five)]";
}
