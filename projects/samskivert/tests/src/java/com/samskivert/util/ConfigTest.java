//
// $Id: ConfigTest.java,v 1.1 2002/03/28 18:57:34 mdb Exp $

package com.samskivert.util;

import java.io.IOException;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link Config} class.
 */
public class ConfigTest extends TestCase
{
    public ConfigTest ()
    {
        super(ConfigTest.class.getName());
    }

    public void runTest ()
    {
        Config config = new Config();
        try {
            config.bindProperties("test", "rsrc/util/test");

            System.out.println("test.prop1: " +
                               config.getValue("test.prop1", 1));
            System.out.println("test.prop2: " +
                               config.getValue("test.prop2", "two"));

            int[] ival = new int[] { 1, 2, 3 };
            ival = config.getValue("test.prop3", ival);
            System.out.println("test.prop3: " + StringUtil.toString(ival));

            String[] sval = new String[] { "one", "two", "three" };
            sval = config.getValue("test.prop4", sval);
            System.out.println("test.prop4: " + StringUtil.toString(sval));

            System.out.println("test.prop5: " +
                               config.getValue("test.prop5", "undefined"));

            Iterator iter = config.keys("test.prop2");
            while (iter.hasNext()) {
                System.out.println(iter.next());
            }

            iter = config.keys("test.prop");
            while (iter.hasNext()) {
                System.out.println(iter.next());
            }

            iter = config.keys("test");
            while (iter.hasNext()) {
                System.out.println(iter.next());
            }

        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    public static Test suite ()
    {
        return new ConfigTest();
    }

    public static void main (String[] args)
    {
        ConfigTest test = new ConfigTest();
        test.runTest();
    }
}
