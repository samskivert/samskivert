//
// $Id: IntegerTableCellRenderer.java,v 1.2 2004/05/18 19:08:45 ray Exp $

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

    // documentation inherited
    protected void setValue (Object value)
    {
        if ((value instanceof Integer) || (value instanceof Long)) {
            setText(_nfi.format(value));
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
