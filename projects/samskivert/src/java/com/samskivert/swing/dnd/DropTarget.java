//
// $Id: DropTarget.java,v 1.1 2002/08/20 02:49:19 ray Exp $

package com.samskivert.swing.dnd;

/**
 * A drop target is associated with a component and represents a valid
 * place to end a drag and drop.
 */
public interface DropTarget
{
    /**
     * Return true if the specified drop should be allowed on this target.
     */
    public boolean checkDrop (DragSource source, Object peekdata);

    /**
     * Called when the drop is actually executed.
     */
    public void dropCompleted (Object data);
}
