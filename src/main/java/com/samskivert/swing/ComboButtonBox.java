//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.samskivert.swing.util.SwingUtil;

/**
 * Used to display a horizontal or vertical array of buttons, out of which
 * only one is selectable at a time (which will be represented by
 * rendering it with an indented border, whereas the other buttons will
 * render with an extruded border.
 */
public class ComboButtonBox extends JPanel
    implements SwingConstants, ListDataListener, MouseListener
{
    /**
     * Constructs a button box with the specified orientation (either
     * {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    public ComboButtonBox (int orientation)
    {
        this(orientation, new DefaultComboBoxModel());
    }

    /**
     * Constructs a button box with the specified orientation (either
     * {@link #HORIZONTAL} or {@link #VERTICAL}. The supplied model will
     * be used to populate the buttons (see {@link #setModel} for more
     * details).
     */
    public ComboButtonBox (int orientation, ComboBoxModel model)
    {
        // set up our layout
        setOrientation(orientation);

        // set up our contents
        setModel(model);
    }

    /**
     * Sets the orientation of the box (either {@link #HORIZONTAL} or
     * {@link #VERTICAL}.
     */
    public void setOrientation (int orientation)
    {
        GroupLayout gl = (orientation == HORIZONTAL) ?
            (GroupLayout)new HGroupLayout() : new VGroupLayout();
        gl.setPolicy(GroupLayout.EQUALIZE);
        setLayout(gl);
    }

    /**
     * Provides the button box with a data model which it will display. If
     * the model contains {@link Image} objects, they will be used to make
     * icons for the buttons. Otherwise the button text will contain the
     * string representation of the elements in the model.
     */
    public void setModel (ComboBoxModel model)
    {
        // if we had a previous model, unregister ourselves from it
        if (_model != null) {
            _model.removeListDataListener(this);
        }

        // subscribe to our new model
        _model = model;
        _model.addListDataListener(this);

        // rebuild the list
        removeAll();
        addButtons(0, _model.getSize());
    }

    @Override
    public void setEnabled (boolean enabled)
    {
        super.setEnabled(enabled);

        int ccount = getComponentCount();
        for (int i = 0; i < ccount; i++) {
            getComponent(i).setEnabled(enabled);
        }
    }

    /**
     * Returns the model in use by the button box.
     */
    public ComboBoxModel getModel ()
    {
        return _model;
    }

    /**
     * Sets the index of the selected component. A value of -1 will clear
     * the selection.
     */
    public void setSelectedIndex (int selidx)
    {
        // update the display
        updateSelection(selidx);

        // let the model know what's up
        Object item = (selidx == -1) ? null : _model.getElementAt(selidx);
        _model.setSelectedItem(item);
    }

    /**
     * Returns the index of the selected item.
     */
    public int getSelectedIndex ()
    {
        return _selectedIndex;
    }

    /**
     * Specifies the command that will be used when generating action
     * events (which is done when the selection changes).
     */
    public void setActionCommand (String actionCommand)
    {
        _actionCommand = actionCommand;
    }

    /**
     * Adds a listener to our list of entities to be notified when the
     * selection changes.
     */
    public void addActionListener (ActionListener l)
    {
        listenerList.add(ActionListener.class, l);
    }

    /**
     * Removes a listener from the list.
     */
    public void removeActionListener (ActionListener l)
    {
        listenerList.remove(ActionListener.class, l);
    }

    /**
     * Notifies our listeners when the selection changed.
     */
    protected void fireActionPerformed ()
    {
        // guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // process the listeners last to first, notifying those that are
        // interested in this event
        for (int i = listeners.length-2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                // lazily create the event:
                if (_actionEvent == null) {
                    _actionEvent = new ActionEvent(
                        this, ActionEvent.ACTION_PERFORMED, _actionCommand);
                }
                ((ActionListener)listeners[i+1]).actionPerformed(_actionEvent);
            }
        }
    }

    // documentation inherited from interface
    public void contentsChanged (ListDataEvent e)
    {
        // if this update is informing us of a new selection, reflect that
        // in the UI
        int start = e.getIndex0(), count = start - e.getIndex1() + 1;
        if (start == -1) {
            // figure out the selected index
            int selidx = -1;
            Object eitem = _model.getSelectedItem();
            if (eitem != null) {
                int ecount = _model.getSize();
                for (int i = 0; i < ecount; i++) {
                    if (eitem == _model.getElementAt(i)) {
                        selidx = i;
                        break;
                    }
                }
            }

            // and update it
            updateSelection(selidx);

        } else {
            // replace the buttons in this range
            removeButtons(start, count);
            addButtons(start, count);
        }
   }

    // documentation inherited from interface
    public void intervalAdded (ListDataEvent e)
    {
        int start = e.getIndex0(), count = e.getIndex1() - start + 1;

        // adjust the selected index
        if (_selectedIndex >= start) {
            _selectedIndex += count;
        }

        // add buttons for the new interval
        addButtons(start, count);
    }

    // documentation inherited from interface
    public void intervalRemoved (ListDataEvent e)
    {
        // remove the buttons in the specified interval
        int start = e.getIndex0(), count = e.getIndex1() - start + 1;
        removeButtons(start, count);
        SwingUtil.refresh(this);
    }

    // documentation inherited from interface
    public void mouseClicked (MouseEvent e)
    {
    }

    // documentation inherited from interface
    public void mousePressed (MouseEvent e)
    {
        // ignore if we're not enabled
        if (!isEnabled()) {
            return;
        }

        // keep track of the selected button
        _selectedButton = (JLabel)e.getSource();
        // if the selected button is already selected, ignore the click
        if (_selectedButton.getBorder() == SELECTED_BORDER) {
            _selectedButton = null;
        } else {
            _selectedButton.setBorder(SELECTED_BORDER);
            _selectedButton.repaint();
        }
    }

    // documentation inherited from interface
    public void mouseReleased (MouseEvent e)
    {
        // if the mouse was released within the bounds of the button, go
        // ahead and select it properly
        if (_selectedButton != null) {
            if (_selectedButton.contains(e.getX(), e.getY())) {
                // tell the model that the selection has changed (and
                // we'll respond and do our business
                Object elem = _selectedButton.getClientProperty("element");
                _model.setSelectedItem(elem);

            } else {
                _selectedButton.setBorder(DESELECTED_BORDER);
                _selectedButton.repaint();
            }

            // clear out the selected button indicator
            _selectedButton = null;
        }
    }

    // documentation inherited from interface
    public void mouseEntered (MouseEvent e)
    {
    }

    // documentation inherited from interface
    public void mouseExited (MouseEvent e)
    {
    }

    /**
     * Adds buttons for the specified range of model elements.
     */
    protected void addButtons (int start, int count)
    {
        Object selobj = _model.getSelectedItem();
        for (int i = start; i < count; i++) {
            Object elem = _model.getElementAt(i);
            if (selobj == elem) {
                _selectedIndex = i;
            }
            JLabel ibut = null;
            if (elem instanceof Image) {
                ibut = new JLabel(new ImageIcon((Image)elem));
            } else {
                ibut = new JLabel(elem.toString());
            }
            ibut.putClientProperty("element", elem);
            ibut.addMouseListener(this);
            ibut.setBorder((_selectedIndex == i) ?
                           SELECTED_BORDER : DESELECTED_BORDER);
            add(ibut, i);
        }

        SwingUtil.refresh(this);
    }

    /**
     * Removes the buttons in the specified interval.
     */
    protected void removeButtons (int start, int count)
    {
        while (count-- > 0) {
            remove(start);
        }

        // adjust the selected index
        if (_selectedIndex >= start) {
            if (start + count > _selectedIndex) {
                _selectedIndex = -1;
            } else {
                _selectedIndex -= count;
            }
        }
    }

    /**
     * Sets the selection to the specified index and updates the buttons
     * to reflect the change. This does not update the model.
     */
    protected void updateSelection (int selidx)
    {
        // do nothing if this element is already selected
        if (selidx == _selectedIndex) {
            return;
        }

        // unhighlight the old component
        if (_selectedIndex != -1) {
            JLabel but = (JLabel)getComponent(_selectedIndex);
            but.setBorder(DESELECTED_BORDER);
        }

        // save the new selection
        _selectedIndex = selidx;

        // if the selection is valid, highlight the new component
        if (_selectedIndex != -1) {
            JLabel but = (JLabel)getComponent(_selectedIndex);
            but.setBorder(SELECTED_BORDER);
        }

        // fire an action performed to let listeners know about our
        // changed selection
        fireActionPerformed();

        repaint();
    }

    /** The contents of the box. */
    protected ComboBoxModel _model;

    /** The index of the selected button. */
    protected int _selectedIndex = -1;

    /** The button over which the mouse was pressed. */
    protected JLabel _selectedButton;

    /** Used when notifying our listeners. */
    protected ActionEvent _actionEvent;

    /** The action command we generate when the selection changes. */
    protected String _actionCommand;

    /** The border used for selected components. */
    protected static final Border SELECTED_BORDER =
        BorderFactory.createLoweredBevelBorder();

    /** The border used for non-selected components. */
    protected static final Border DESELECTED_BORDER =
        BorderFactory.createRaisedBevelBorder();
}
