//
// $Id: FontPicker.java,v 1.1 2001/07/17 06:01:08 mdb Exp $

package com.samskivert.viztool.viz;

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
        int size = printing ? 8 : 10;
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
