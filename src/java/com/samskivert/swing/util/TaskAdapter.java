//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

package com.samskivert.swing.util;

/**
 * A helper class for easily instantiating {@link Task} instances.
 */
public class TaskAdapter implements Task
{
    /**
     * Always returns null by default. Override this method to implement
     * the desired functionality.
     *
     * @see Task#invoke
     */
    public Object invoke () throws Exception
    {
        return null;
    }

    /**
     * Always returns false by default. Override this method to implement
     * the desired functionality.
     *
     * @see Task#abort
     */
    public boolean abort ()
    {
        return false;
    }
}
