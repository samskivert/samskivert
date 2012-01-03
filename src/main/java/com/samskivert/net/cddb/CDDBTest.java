//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.net.cddb;

/**
 * Tests the CDDB code by connecting to a CDDB server and making some
 * requests.
 */
public class CDDBTest
{
    public static void test (String hostname, String cdid)
        throws Exception
    {
        CDDB cddb = new CDDB();

        try {
            /* String rsp = */ cddb.connect(hostname);

            // set the timeout to 30 seconds
            cddb.setTimeout(30*1000);

            // try a test query
            int[] offsets = { 150, 18130, 48615 };
            int length = 893;
            CDDB.Entry[] entries = cddb.query(cdid, offsets, length);

            if (entries == null || entries.length == 0) {
                System.out.println("No match for " + cdid + ".");

            } else {
                for (int i = 0; i < entries.length; i++) {
                    System.out.println("Match " + entries[i].category + "/" +
                    entries[i].cdid + "/" +
                    entries[i].title);
                }

                CDDB.Detail detail = cddb.read(entries[0].category,
                entries[0].cdid);
                System.out.println("Title: " + detail.title);
                for (int i = 0; i < detail.trackNames.length; i++) {
                    System.out.println(pad(i) + ": " + detail.trackNames[i]);
                }
                System.out.println("Extended data: " + detail.extendedData);
                for (int i = 0; i < detail.extendedTrackData.length; i++) {
                    System.out.println(pad(i) + ": " + detail.extendedTrackData[i]);
                }
            }

        } finally {
            cddb.close();
        }
    }

    protected static String pad (int value)
    {
        return ((value > 9) ? "" : " ") + value;
    }

    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: CDDBTest cddbhost");
            System.exit(-1);
        }

        try {
            test(args[0], "1b037b03");

        } catch (CDDBException ce) {
            System.err.println("Protocol exception: " + ce.getCode() + ": " + ce.getMessage());

        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }
}
