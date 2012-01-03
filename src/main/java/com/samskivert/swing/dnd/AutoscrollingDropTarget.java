//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
