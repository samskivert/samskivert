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

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.samskivert.servlet.MessageManager;

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
                testParse(template);
            } catch (Exception e) {
                fail("Failed to process " + template + ": " + e);
            }
        }

        // then try to actually merge them with a context
        for (String template : getTemplates()) {
            try {
                testMerge(template);
            } catch (Exception e) {
                fail("Failed to process " + template + ": " + e);
            }
        }
    }

    protected void testParse (String template)
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

    protected void testMerge (String template)
        throws Exception
    {
        // clear out any previous logging output
        _logbuf.getBuffer().setLength(0);

        // populate the context with useful bits
        VelocityContext ctx = new VelocityContext();
        populateContext(template, ctx);

        // now merge the template
        StringWriter writer = new StringWriter();
        _engine.mergeTemplate(template, ctx, writer);

        // if there was any logging output, the test failed
        String logout = _logbuf.toString();
        if (logout.length() > 0) {
            fail("Template merge failed '" + template + "'.\n" + logout);
        }
    }

    /**
     * Called before parsing each template to populate the context as needed
     * for the template in question. The default implementation adds some
     * standard bits provided by {@link DispatcherServlet}.
     */
    protected void populateContext (String template, VelocityContext ctx)
    {
        ctx.put("context_path", "/test");

        // populate the context with various standard tools
        MessageManager msgmgr = getMessageManager();
        if (msgmgr != null) {
            ctx.put(DispatcherServlet.I18NTOOL_KEY, new I18nTool(null, msgmgr) {
                protected Locale getLocale () {
                    return Locale.getDefault();
                }
            });
        }
        ctx.put(DispatcherServlet.FORMTOOL_KEY, new FormTool(null) {
            protected String getParameter (String name) {
                return null;
            }
        });
        ctx.put(DispatcherServlet.STRINGTOOL_KEY, new StringTool());
        ctx.put(DispatcherServlet.DATATOOL_KEY, new DataTool());
        ctx.put(DispatcherServlet.CURRENCYTOOL_KEY,
                new CurrencyTool(Locale.getDefault()));
    }

    /**
     * If the tests require translation, this method must return a message
     * manager configured appropriately.
     */
    protected MessageManager getMessageManager ()
    {
        return null;
    }

    /**
     * Returns an array of template paths which will be resolved on the
     * classpath.
     */
    protected abstract String[] getTemplates ();

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
