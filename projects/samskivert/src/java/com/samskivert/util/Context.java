//
// $Id: Context.java,v 1.2 2001/08/11 22:43:29 mdb Exp $
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
