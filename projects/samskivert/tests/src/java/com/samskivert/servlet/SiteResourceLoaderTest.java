//
// $Id: SiteResourceLoaderTest.java,v 1.4 2001/12/13 01:31:23 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

package com.samskivert.servlet;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.util.StreamUtils;

import com.samskivert.Log;
import com.samskivert.test.TestUtil;

public class SiteResourceLoaderTest extends TestCase
{
    public SiteResourceLoaderTest ()
    {
        super(SiteResourceLoaderTest.class.getName());
    }

    public void runTest ()
    {
        // we need to fake a couple of things to get the test to work
        TestSiteIdentifier ident = new TestSiteIdentifier();

        // now create a resource loader and load up some resources
        SiteResourceLoader loader = new SiteResourceLoader(
            ident, TestUtil.getResourcePath("rsrc/servlet/srl"));

        try {
            testResourceLoader(SiteIdentifier.DEFAULT_SITE_ID,
                               loader, "defaultout.txt");
            testResourceLoader(SITE1_ID, loader, "site1out.txt");
            testResourceLoader(SITE2_ID, loader, "site2out.txt");

        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("Caught exception while testing resource loader.");
        }
    }

    protected void testResourceLoader (
        int siteId, SiteResourceLoader loader, String compareFile)
        throws IOException
    {
        StringBuffer gen = new StringBuffer();
        appendResource(siteId, gen, loader, "/header.txt");
        appendResource(siteId, gen, loader, "/body.txt");
        appendResource(siteId, gen, loader, "/footer.txt");

        StringBuffer cmp = new StringBuffer();
        compareFile = "rsrc/servlet/srl/" + compareFile;
        InputStream cin = TestUtil.getResourceAsStream(compareFile);
        if (cin == null) {
            throw new IOException("Unable to load " + compareFile);
        }
        cmp.append(StreamUtils.streamAsString(cin));

        // Log.info("Loaded resources [cmp=" + compareFile + "]: " + gen);

        // now make sure the strings match
        assertEquals("Testing " + compareFile,
                     cmp.toString(), gen.toString());
    }

    protected void appendResource (int siteId, StringBuffer buffer,
                                   SiteResourceLoader loader, String path)
        throws IOException
    {
        InputStream rin = null;

        // load up the site-specific resource
        try {
            rin = loader.getResourceAsStream(siteId, path);
        } catch (FileNotFoundException fnfe) {
            // fall through
        }

        if (rin == null) {
            // fall back to the "default" resource if we couldn't load a
            // site-specific version
            String rpath = "rsrc/servlet/srl/default/" + path;
            rpath = TestUtil.getResourcePath(rpath);
            rin = new FileInputStream(rpath);
        }
        buffer.append(StreamUtils.streamAsString(rin));
    }

    public static Test suite ()
    {
        return new SiteResourceLoaderTest();
    }

    public static class TestSiteIdentifier implements SiteIdentifier
    {
        public int identifySite (HttpServletRequest req)
        {
            return DEFAULT_SITE_ID;
        }

        public String getSiteString (int siteId)
        {
            switch (siteId) {
            case SITE1_ID: return "site1";
            case SITE2_ID: return "site2";
            default: return DEFAULT_SITE_STRING;
            }
        }
    }

    protected static final int SITE1_ID = 1;
    protected static final int SITE2_ID = 2;
}
