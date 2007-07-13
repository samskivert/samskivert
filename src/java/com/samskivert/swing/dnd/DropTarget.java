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

import java.awt.Point;

/**
 * A drop target is associated with a component and represents a valid
 * place to end a drag and drop.
 */
public interface DropTarget
{
    /**
     * Return true if the specified drop should be allowed on this target.
     * Also indicates that a hover has started in case the drop target would
     * like to create dragover feedback aside from the cursor feedback.
     *
     * Called after and only if the DragSource's checkDrop returned true.
     * If this method returns true then either {@link #dropCompleted} or
     * {@link #noDrop} will be called next.
     */
    public boolean checkDrop (DragSource source, Object peekdata);

    /**
     * Called to let the DropTarget know that we've stopped hovering over
     * but did not actually drop.
     */
    public void noDrop ();

    /**
     * Called when the drop is actually executed.
     *
     * @param pos the location of the drop in screen coordinates
     */
    public void dropCompleted (DragSource source, Object data, Point pos);
}
