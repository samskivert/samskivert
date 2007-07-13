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

package com.samskivert.swing.dnd;

import java.awt.Dimension;

/**
 * An extension of DropTarget that allows its associated component to be
 * automatically scrolled during DnD operations. So easy!
 */
public interface AutoscrollingDropTarget extends DropTarget
{
    /**
     * Return a dimension that represents the horizontal and vertical
     * 'sensitive' area around the component's bounds. When the mouse leaves
     * the component and is inside the sensitive area, the component is
     * autoscrolled.
     */
    public Dimension getAutoscrollBorders ();
}
