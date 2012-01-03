//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
