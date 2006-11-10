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
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * An abstract base class for easily testing Velocity templates.
 */
public abstract class VelocityTestCase extends TestCase
{
    protected VelocityTestCase (String name)
    {
        super(name);
    }

    @Override // from TestCase
    protected void setUp ()
    {
        try {
            _engine = new VelocityEngine();
            _engine.setProperty(VelocityEngine.VM_LIBRARY, "");
            _engine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
            _engine.setProperty(
                "classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
                ClasspathResourceLoader.class.getName());
            _engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, _logger);
            _engine.init();

        } catch (Exception e) {
            fail("Velocity initialization failed " + e);
        }
    }

    @Override // from TestCase
    protected void runTest ()
    {
        // first simply evaluate all of the templates
        for (String template : getTemplates()) {
            try {
                evaluateTemplate(template);
            } catch (Exception e) {
                fail("Failed to process " + template + ": " + e);
            }
        }

        // then try to actually merge them with a context
        for (String template : getTemplates()) {
            try {
                mergeTemplate(template);
            } catch (Exception e) {
                fail("Failed to process " + template + ": " + e);
            }
        }
    }

    protected void evaluateTemplate (String template)
        throws Exception
    {
        InputStream tempin = 
            getClass().getClassLoader().getResourceAsStream(template);
        if (tempin == null) {
            fail("Missing template '" + template + "'.");
        }

        // parse the template
        if (!_engine.evaluate(new VelocityContext(), new StringWriter(),
                              template, tempin)) {
            fail("Template parsing failed '" + template + "'.");
        }
    }

    protected void mergeTemplate (String template)
        throws Exception
    {
        // first load our golden output
        String goldpath = getGoldenFilePath(template);
        InputStream goldin =
            getClass().getClassLoader().getResourceAsStream(goldpath);
        if (goldin == null) {
            // if there is no golden template, we don't test this file
            return;
        }
        String goldtext = IOUtils.toString(goldin);

        // clear out any previous logging output
        _logbuf.getBuffer().setLength(0);

        // now generate the test output
        VelocityContext context = new VelocityContext();
        populateContext(template, context);
        StringWriter writer = new StringWriter();
        _engine.mergeTemplate(template, context, writer);

        // now compare the two
        String output = "Template output incorrect [template=" + template +
            ", golden=" + goldpath + "].";
        String logout = _logbuf.toString();
        if (logout.length() > 0) {
            output = output + "\n" + logout;
        }
        assertEquals(output, goldtext, writer.toString());
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

    /** Accumulates logging to a buffer for later reporting. */
    protected LogChute _logger = new LogChute() {
        public void init (RuntimeServices rs) throws Exception {
            // nothing doing
        }
        public void log (int level, String message) {
            switch (level) {
            case ERROR_ID:
            case WARN_ID:
                _logout.println(message);
                break;
            default:
            case INFO_ID:
            case DEBUG_ID:
                // velocity insists on sending info and debug messages even
                // though isLevelEnabled() returns false
                break;
            }
        }
        public void log (int level, String message, Throwable t) {
            log(level, message);
            t.printStackTrace(_logout);
        }
        public boolean isLevelEnabled (int level) {
            return (level == WARN_ID || level == ERROR_ID);
        }
    };

    protected VelocityEngine _engine;

    protected StringWriter _logbuf = new StringWriter();
    protected PrintWriter _logout = new PrintWriter(_logbuf);
}
