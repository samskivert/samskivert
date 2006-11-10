//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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

package com.samskivert.velocity;

import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * An abstract base class for easily testing Velocity templates.
 */
public abstract class VelocityTestCase extends TestCase
{
    protected VelocityTestCase (String name)
    {
        super(name);
    }

    protected VelocityTestCase ()
    {
        this(VelocityTestCase.class.getName());
    }

    @Override // from TestCase
    protected void setUp ()
    {
        try {
            _engine = VelocityUtil.createEngine();
        } catch (Exception e) {
            fail("Velocity initialization failed " + e);
        }
    }

    @Override // from TestCase
    protected void runTest ()
    {
        for (String template : getTemplates()) {
            try {
                runTest(template);
            } catch (Exception e) {
                fail("Failed to process " + template + ": " + e);
            }
        }
    }

    protected void runTest (String template)
        throws Exception
    {
        // first load our golden output
        String goldpath = getGoldenFilePath(template);
        InputStream goldin =
            getClass().getClassLoader().getResourceAsStream(goldpath);
        if (goldin == null) {
            fail("Missing golden file for " + template +
                 " [path=" + goldpath + "].");
        }
        String goldtext = IOUtils.toString(goldin);

        // now generate the test output
        VelocityContext context = new VelocityContext();
        populateContext(template, context);
        StringWriter writer = new StringWriter();
        _engine.mergeTemplate(template, context, writer);

        // and compare the two
        assertEquals("Template output incorrect [template=" + template +
                     ", golden=" + goldpath + "].",
                     goldtext, writer.toString());
    }

    /**
     * Returns an array of template paths which will be resolved on the
     * classpath.
     */
    protected abstract String[] getTemplates ();

    /**
     * Called before parsing each template to populate the context as needed
     * for the template in question. The default implementation adds nothing to
     * the context.
     */
    protected void populateContext (String template, VelocityContext context)
    {
    }

    /**
     * Returns the path (resolved via the classloader) of the golden file
     * against which to compare the output of the supplied template. The
     * default simply appends .test to the path.
     */
    protected String getGoldenFilePath (String template)
    {
        return template + ".test";
    }

    protected VelocityEngine _engine;
}
