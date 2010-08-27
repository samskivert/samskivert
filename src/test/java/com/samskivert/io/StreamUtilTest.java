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
