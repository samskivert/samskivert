//
// $Id: Context.java,v 1.1 2001/07/20 19:46:56 mdb Exp $

package com.samskivert.util;

/**
 * The context interface serves as a basis for a system whereby an
 * application that makes use of a number of decoupled services, can
 * provide implementations of components needed by those services and do
 * with a single object that implements a host of different
 * <code>Context</code> interfaces, each of which encapsulates the needs
 * of a particular component. This is hard to explain in the abstract, and
 * easy to understand in the concrete, but this is documentation and I'm
 * lazy, so you'll have to try to cope with the abstract.
 */
public interface Context
{
    /**
     * Returns a reference to the config object in use in this application.
     */
    public Config getConfig ();
}
