//
// $Id: DropTarget.java,v 1.4 2002/08/21 17:54:10 mdb Exp $

package com.samskivert.swing.dnd;

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
     */
    public void dropCompleted (DragSource source, Object data);
}
