//
// $Id: DummyLogic.java,v 1.3 2001/08/11 22:43:29 mdb Exp $
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

package com.samskivert.webmacro;

import org.webmacro.servlet.WebContext;

/**
 * The dummy logic is used as a placeholder for URIs that match no normal
 * logic or for which the matching logic could not be instantiated. It
 * does nothing.
 */
public class DummyLogic implements Logic
{
    public void invoke (Application app, WebContext context) throws Exception
    {
	// we're such a dummy that we do absolutely nothing.
    }
}
