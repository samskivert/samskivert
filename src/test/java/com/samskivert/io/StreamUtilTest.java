//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link StreamUtil} class.
 */
public class StreamUtilTest
{
    @Test public void testCopy ()
        throws Exception
    {
        assertTrue(PHRASE.equals(copyString(PHRASE)));
        String bulk = getBigText();
        assertTrue(bulk.equals(copyString(bulk)));
    }

    @Test public void testToString ()
        throws Exception
    {
        byte[] data = PHRASE.getBytes("UTF-8");
        assertTrue(PHRASE.equals(StreamUtil.toString(new ByteArrayInputStream(data), "UTF-8")));
        String text = getBigText();
        data = text.getBytes("UTF-8");
        assertTrue(text.equals(StreamUtil.toString(new ByteArrayInputStream(data), "UTF-8")));
    }

    protected String getBigText ()
    {
        StringBuffer buf = new StringBuffer();
        for (int ii = 0; ii < 10000; ii++) {
            buf.append(PHRASE).append("\n");
        }
        return buf.toString();
    }

    protected String copyString (String text)
        throws Exception
    {
        ByteArrayInputStream bin = new ByteArrayInputStream(text.getBytes("UTF-8"));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamUtil.copy(bin, bout);
        return bout.toString("UTF-8");
    }

    protected static final String PHRASE =
        "Now is the time for all good men\nto come to the aid of their government.";
}
