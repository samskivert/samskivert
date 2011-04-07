//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

public class SystemInfoDemo
{
    public static void main (String[] args)
    {
        // output initial system information
        SystemInfo info = new SystemInfo();
        System.out.println("Initial system info:");
        System.out.println(info.toString());

        // allocate a bit of data
        System.out.println("\nAllocating test data.");
        for (int ii = 0; ii < 10000; ii++) {
            _counter += new int[100].length;
        }

        // update and output the latest system information
        info.update();
        System.out.println("\nUpdated system info:");
        System.out.println(info.toString());
    }

    protected static long _counter;
}
