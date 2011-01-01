//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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
