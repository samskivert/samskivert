//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A table cell renderer that that formats integers according to the
 * locale's desires.
 */
public class IntegerTableCellRenderer extends DefaultTableCellRenderer
{
    public IntegerTableCellRenderer ()
    {
        setHorizontalAlignment(RIGHT);
    }

    @Override
    protected void setValue (Object value)
    {
        if ((value instanceof Integer) || (value instanceof Long)) {
            setText(_nfi.format(value) + " ");
        } else {
            super.setValue(value);
        }
    }

    // our number formatter
    protected NumberFormat _nfi = NumberFormat.getIntegerInstance();

    /**
     * A convenience method for installing this renderer.
     */
    public static void install (JTable table)
    {
        table.setDefaultRenderer(Number.class, new IntegerTableCellRenderer());
    }
}
