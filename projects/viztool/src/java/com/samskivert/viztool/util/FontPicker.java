//
// $Id: FontPicker.java,v 1.4 2001/12/01 05:28:01 mdb Exp $
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.viztool.util;

import java.awt.Font;

/**
 * The font picker provides the proper font based on operating condtions.
 */
public class FontPicker
{
    /**
     * Instructs the font picker to choose fonts for printing or fonts for
     * displaying on the screen based on the value of the
     * <code>printing</code> argument.
     */
    public static void init (boolean printing)
    {
        int size = printing ? 8 : 12;
        _titleFont = new Font("Helvetica", Font.BOLD, size);
        _classFont = new Font("Helvetica", Font.PLAIN, size);
        _ifaceFont = new Font("Helvetica", Font.ITALIC, size);
        _implsFont = new Font("Helvetica", Font.ITALIC, size-2);
        _declsFont = new Font("Helvetica", Font.PLAIN, size-2);
    }

    public static Font getTitleFont ()
    {
        return _titleFont;
    }

    public static Font getClassFont ()
    {
        return _classFont;
    }

    public static Font getInterfaceFont ()
    {
        return _ifaceFont;
    }

    public static Font getImplementsFont ()
    {
        return _implsFont;
    }

    public static Font getDeclaresFont ()
    {
        return _declsFont;
    }

    protected static Font _titleFont;
    protected static Font _classFont;
    protected static Font _ifaceFont;
    protected static Font _implsFont;
    protected static Font _declsFont;
}
