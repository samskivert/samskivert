//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.junit.*;
import static org.junit.Assert.*;

import com.samskivert.io.StreamUtil;
import com.samskivert.test.TestUtil;

public class SiteResourceLoaderTest
{
    @Test
    public void runTest ()
    {
        // we need to fake a couple of things to get the test to work
        TestSiteIdentifier ident = new TestSiteIdentifier();

        // now create a resource loader and load up some resources
        SiteResourceLoader loader = new SiteResourceLoader(
            ident, TestUtil.getResourcePath("servlet/srl"));

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

    protected void testResourceLoader (int siteId, SiteResourceLoader loader, String compareFile)
        throws IOException
    {
        StringBuffer gen = new StringBuffer();
        appendResource(siteId, gen, loader, "/header.txt");
        appendResource(siteId, gen, loader, "/body.txt");
        appendResource(siteId, gen, loader, "/footer.txt");

        StringBuffer cmp = new StringBuffer();
        compareFile = "servlet/srl/" + compareFile;
        InputStream cin = TestUtil.getResourceAsStream(compareFile);
        if (cin == null) {
            throw new IOException("Unable to load " + compareFile);
        }
        cmp.append(StreamUtil.toString(cin, "UTF-8"));

        // Log.info("Loaded resources [cmp=" + compareFile + "]: " + gen);

        // now make sure the strings match
        assertEquals("Testing " + compareFile, cmp.toString(), gen.toString());
    }

    protected void appendResource (
        int siteId, StringBuffer buffer, SiteResourceLoader loader, String path)
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
            String rpath = "servlet/srl/default/" + path;
            rpath = TestUtil.getResourcePath(rpath);
            rin = new FileInputStream(rpath);
        }
        buffer.append(StreamUtil.toString(rin, "UTF-8"));
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

        public int getSiteId (String siteString)
        {
            if ("site1".equals(siteString)) {
                return SITE1_ID;
            } else if ("site2".equals(siteString)) {
                return SITE2_ID;
            } else {
                return DEFAULT_SITE_ID;
            }
        }

        public Iterator<Site> enumerateSites ()
        {
            return null; // not used
        }
    }

    protected static final int SITE1_ID = 1;
    protected static final int SITE2_ID = 2;
}
