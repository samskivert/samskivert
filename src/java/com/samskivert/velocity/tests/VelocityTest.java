//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2008 Michael Bayne
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

package com.samskivert.velocity.tests;

import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import org.junit.*;
import static org.junit.Assert.*;

import com.samskivert.velocity.VelocityUtil;

/**
 * Tests some Velocity stuff.
 */
public class VelocityTest
{
    @Test
    public void testClasspathLoader ()
        throws Exception
    {
        VelocityContext ctx = new VelocityContext();
        ctx.put("foo", "bar");

        StringWriter writer = new StringWriter();
        VelocityEngine engine = VelocityUtil.createEngine();
        engine.mergeTemplate("com/samskivert/velocity/tests/test.tmpl", "UTF-8", ctx, writer);

        assertTrue(writer.toString().trim().equals("Hello bar."));
    }
}
