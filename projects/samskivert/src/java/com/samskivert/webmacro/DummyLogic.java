//
// $Id: DummyLogic.java,v 1.1 2001/03/01 21:06:22 mdb Exp $

package com.samskivert.webmacro;

import org.webmacro.servlet.WebContext;

/**
 * The dummy logic is used as a placeholder for URIs that match no normal
 * logic or for which the matching logic could not be instantiated. It
 * does nothing.
 */
public class DummyLogic implements Logic
{
    public void invoke (WebContext context) throws Exception
    {
	// we're such a dummy that we do absolutely nothing.
    }
}
