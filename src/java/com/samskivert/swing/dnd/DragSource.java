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

import java.awt.Cursor;

/**
 * A drag source is associated with a component and represents a valid
 * place to start a drag and drop operation.
 */
public interface DragSource
{
    /**
     * Called by the DnDManager to verify that a drag may begin at this source.
     *
     * @param cursors a two-element array- the first element should contain
     * the cursor to use for valid drops, the second for invalid drops. Either
     * or both may be left blank to use the default DnD cursors.
     * @param data a single-element array that should be filled in with
     * the data that will be sent to a DropTarget if there is a successful
     * drop.
     * @return true if the drag may begin.
     */
    public boolean startDrag (Cursor[] cursors, Object[] data);

    /**
     * Is the drop target an acceptable one?
     * Called prior to calling the DropTarget's checkDrop.
     *
     * @param target a potential DropTarget.
     */
    public boolean checkDrop (DropTarget target);

    /**
     * A callback to let the source know that the drop completed successfully.
     */
    public void dragCompleted (DropTarget target);
}
