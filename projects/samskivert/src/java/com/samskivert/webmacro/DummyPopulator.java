//
// $Id: DummyPopulator.java,v 1.1 2001/02/15 01:44:34 mdb Exp $

package com.samskivert.webmacro;

import org.webmacro.servlet.WebContext;

/**
 * The dummy populator is used as a placeholder for URIs that match no
 * normal populator or for which the matching populator could not be
 * instantiated. It does nothing.
 */
public class DummyPopulator implements ContextPopulator
{
    public void populate (WebContext context) throws Exception
    {
	// we're such a dummy that we do absolutely nothing.
    }
}
