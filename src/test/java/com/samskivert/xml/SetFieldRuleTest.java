//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.xml;

import java.io.InputStream;
import java.io.FileInputStream;

import org.junit.*;
import static org.junit.Assert.*;

import org.apache.commons.digester.Digester;

import com.samskivert.test.TestUtil;
import com.samskivert.util.StringUtil;

public class SetFieldRuleTest
{
    public static class TestObject
    {
        public int intField;
        public String stringField;
        public Integer integerField;
        public int[] intArrayField;
        public String[] stringArrayField;

        @Override public String toString ()
        {
            return "[intField=" + intField + ", stringField=" + stringField +
                ", integerField=" + integerField +
                ", intArrayField=" + StringUtil.toString(intArrayField) +
                ", stringArrayField=" + StringUtil.toString(stringArrayField) +
                "]";
        }
    }

    @Test public void runTest ()
    {
        Digester digester = new Digester();

        // create our object and push it onto the digester
        TestObject object = new TestObject();
        digester.push(object);

        // set up some rules
        digester.addRule("object/intField", new SetFieldRule("intField"));
        digester.addRule("object/stringField", new SetFieldRule("stringField"));
        digester.addRule("object/integerField", new SetFieldRule("integerField"));
        digester.addRule("object/intArrayField", new SetFieldRule("intArrayField"));
        digester.addRule("object/stringArrayField", new SetFieldRule("stringArrayField"));

        try {
            String xmlpath = TestUtil.getResourcePath("xml/setfieldtest.xml");
            InputStream input = new FileInputStream(xmlpath);
            digester.parse(input);
            input.close();
        } catch (Exception e) {
            fail("Parsing failed: " + e);
        }

        assertTrue(EXPECTED.equals(object.toString()));
    }

    protected static final String EXPECTED =
        "[intField=5, stringField=howdy partner!, integerField=15, " +
        "intArrayField=(1, 2, 3, 4, 5), " +
        "stringArrayField=(one, two, three, four, five)]";
}
