//
// $Id: DnDManager.java,v 1.2 2002/08/20 21:05:10 ray Exp $

package com.samskivert.swing.dnd;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.JComponent;

import javax.swing.event.AncestorEvent;

import com.samskivert.swing.event.AncestorAdapter;

/**
 * A custom Drag and Drop manager for use within a single JVM. Does what we
 * need it to do and no more.
 */
public class DnDManager
    implements MouseMotionListener, AWTEventListener
{
    /**
     * Add the specified component as a source of drags, with the DragSource
     * controller.
     */
    public static void addDragSource (DragSource source, JComponent comp)
    {
        singleton.addSource(source, comp);
    }

    /**
     * Add the specified component as a drop target.
     */
    public static void addDropTarget (DropTarget target, JComponent comp)
    {
        singleton.addTarget(target, comp);
    }

    /**
     * Create a custom cursor out of the specified image.
     */
    public static Cursor createImageCursor (Image img)
    {
        // TODO: check colors/size
        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.createCustomCursor(img,
            new Point(img.getWidth(null) / 2, img.getHeight(null) / 2),
            "samskivertDnDCursor");
    }

    /**
     * Restrict construction.
     */
    private DnDManager ()
    {
    }

    /**
     * Add a dragsource.
     */
    protected void addSource (DragSource source, JComponent comp)
    {
        _draggers.put(comp, source);
        comp.addAncestorListener(_remover);
        comp.addMouseMotionListener(this);
    }

    /**
     * Add a droptarget.
     */
    protected void addTarget (DropTarget target, JComponent comp)
    {
        _droppers.put(comp, target);
        comp.addAncestorListener(_remover);
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseMoved (MouseEvent me)
    {
        // who cares.
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseDragged (MouseEvent me)
    {
        // make sure a drag hasn't already started.
        if (_sourceComp != null) {
            return;
        }

        _sourceComp = me.getComponent();
        _source = (DragSource) _draggers.get(_sourceComp);

        // make sure the source wants to start a drag.
        if (!_source.startDrag(_cursors, _data)) {
            // if not, reset our start conditions and bail
            reset();
            return;
        }

        // use standard cursors if custom ones not specified
        if (_cursors[0] == null) {
            _cursors[0] = java.awt.dnd.DragSource.DefaultMoveDrop;
        }
        if (_cursors[1] == null) {
            _cursors[1] = java.awt.dnd.DragSource.DefaultMoveNoDrop;
        }

        // install a listener so we know everywhere that the mouse enters
        Toolkit.getDefaultToolkit().addAWTEventListener(this, 
                AWTEvent.MOUSE_EVENT_MASK);

        // and start out with the no-drop cursor
        _lastComp = _sourceComp;
        _oldCursor = _lastComp.getCursor();
        _lastComp.setCursor(_cursors[1]);
    }

    // documentation inherited from interface AWTEventListener
    public void eventDispatched (AWTEvent event)
    {
        switch (event.getID()) {
        case MouseEvent.MOUSE_ENTERED:
            mouseEntered((MouseEvent) event);
            break;

        case MouseEvent.MOUSE_EXITED:
            mouseExited((MouseEvent) event);
            break;

        case MouseEvent.MOUSE_RELEASED:
            mouseReleased((MouseEvent) event);
            break;
        }
    }

    /**
     * Handle the mouse entering a new component.
     */
    protected void mouseEntered (MouseEvent event)
    {
        Component oldcomp = _lastComp;
        _lastComp = ((MouseEvent) event).getComponent();
        _lastTarget = findAppropriateTarget(_lastComp);
        if (_lastComp != oldcomp) {
            oldcomp.setCursor(_oldCursor);
            _oldCursor = _lastComp.getCursor();
        }
        _lastComp.setCursor(_cursors[(_lastTarget == null) ? 1 : 0]);
    }

    /**
     * Handle the mouse leaving a component.
     */
    protected void mouseExited (MouseEvent event)
    {
        _lastTarget = null;
        if (_lastComp != null) {
            _lastComp.setCursor(_oldCursor);
        }
    }

    /**
     * Handle the mouse button being released.
     */
    protected void mouseReleased (MouseEvent event)
    {
        // since the release comes with a component of the source,
        // we use the last enter...
        if (_lastTarget != null) {
            _lastTarget.dropCompleted(_data[0]);
            _source.dragCompleted(_lastTarget);
        }

        if (_lastComp != null) {
            _lastComp.setCursor(_oldCursor);
        }
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        reset();
    }

    /**
     * Find the lowest accepting parental target to this component.
     */
    protected DropTarget findAppropriateTarget (Component comp)
    {
        Component parent;
        DropTarget target;
        while (true) {
            // here we sneakily prevent dropping on the source
            target = (comp == _sourceComp) ? null
                                           : (DropTarget) _droppers.get(comp);
            if ((target != null) && target.checkDrop(_source, _data[0])) {
                return target;
            }
            parent = comp.getParent();
            if (parent == null) {
                return null;
            }
            comp = parent;
        }
    }

    /**
     * Reset dnd to a starting state.
     */
    protected void reset ()
    {
        _source = null;
        _sourceComp = null;
        _lastComp = null;
        _lastTarget = null;
        _data[0] = null;
        _cursors[0] = null;
        _cursors[1] = null;
    }

    /** A handy helper that removes components when they're no longer in
     * the hierarchy. */
    protected AncestorAdapter _remover = new AncestorAdapter() {
        public void ancestorRemoved (AncestorEvent ae)
        {
            JComponent comp = ae.getComponent();
            _draggers.remove(comp);
            _droppers.remove(comp);
        }
    };

    /** Our DropTargets, indexed by associated Component. */
    protected HashMap _droppers = new HashMap();

    /** Our DragSources, indexed by associated component. */
    protected HashMap _draggers = new HashMap();

    /** The original and last component that the mouse was in during a drag. */
    protected Component _sourceComp, _lastComp;

    /** The source of a drag. */
    protected DragSource _source;

    /** The last target, or null if no last target. */
    protected DropTarget _lastTarget;

    /** The cursor that used to be set for _lastComp. */
    protected Cursor _oldCursor;

    /** The accept/reject cursors. */
    protected Cursor[] _cursors = new Cursor[2];

    /** The data to be passed in the drop. */
    protected Object[] _data = new Object[1];

    /** A single manager for the entire JVM. */
    protected static final DnDManager singleton = new DnDManager();
}
