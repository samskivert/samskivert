//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;

/**
 * Displays a list of components in sections (with section headers) that
 * can be collapsed. Each section of a list uses a {@link JList} instance
 * to render the section elements.
 */
public class CollapsibleList<E> extends JPanel
{
    /**
     * Constructs an empty collapsible list.
     */
    public CollapsibleList ()
    {
        // set up our layout manager
        VGroupLayout gl = new VGroupLayout(VGroupLayout.NONE);
        gl.setOffAxisPolicy(VGroupLayout.STRETCH);
        gl.setJustification(VGroupLayout.TOP);
        gl.setOffAxisJustification(VGroupLayout.LEFT);
        setLayout(gl);
    }

    /**
     * Constructs a collapsible list with the supplied section labels and models.
     */
    public CollapsibleList (List<String> sections, List<ListModel<E>> models)
    {
        this(); // set up our layout manager

        for (int ii = 0, ll = sections.size(); ii < ll; ii++) {
            addSection(sections.get(ii), models.get(ii));
        }
    }

    /**
     * Returns the number of sections.
     */
    public int getSectionCount ()
    {
        return getComponentCount()/2;
    }

    /**
     * Adds a section to this collapsible list.
     *
     * @param label the title of the section.
     * @param model the list model to use for the new section.
     *
     * @return the index of the newly added section.
     */
    public int addSection (String label, ListModel<E> model)
    {
        add(new JLabel(label));
        add(new JList<E>(model));
        return getSectionCount()-1;
    }

    /**
     * Returns the label object associated with the title of the specified
     * section.
     */
    public JLabel getSectionLabel (int index)
    {
        return (JLabel)getComponent(index*2);
    }

    /**
     * Returns the list object associated with the specified section.
     */
    public JList<E> getSectionList (int index)
    {
        @SuppressWarnings("unchecked") JList<E> list = (JList<E>)getComponent(index*2+1);
        return list;
    }

    /**
     * Toggles the collapsed state of the specified section.
     */
    public void toggleCollapsed (int index)
    {
        JList<E> list = getSectionList(index);
        list.setVisible(!list.isVisible());
    }
}
