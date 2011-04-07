//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import org.junit.*;
import static org.junit.Assert.*;

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
        engine.mergeTemplate("velocity/test.tmpl", "UTF-8", ctx, writer);

        assertTrue(writer.toString().trim().equals("Hello bar."));
    }
}
