//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

package com.samskivert.swing;

/**
 * The controller provider interface is implemented by user interface
 * elements that have an associated {@link
 * com.samskivert.swing.Controller}. The hierarchy of the user interface
 * elements defines the hierarchy of controllers and at any point in the
 * UI element hierarchy, an element can implement controller provider and
 * provide a controller that will process actions received at that scope
 * or below.
 */
public interface ControllerProvider
{
    /**
     * Returns the controller to be used at the scope of the UI element
     * that implements controller provider.
     */
    public Controller getController ();
}
