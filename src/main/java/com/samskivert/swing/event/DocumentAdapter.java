//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.event;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A DocumentAdapter for focusing DocumentListener events into a pinpoint
 * of easy wonderosity.
 * Or you can override each of the DocumentListener methods as you wish.
 */
public class DocumentAdapter implements DocumentListener
{
    /**
     * A handy-dandy method you can override to just do *something* whenever
     * the document changes.
     */
    public void documentChanged ()
    {
    }

    // documentation inherited from interface DocumentListener
    public void changedUpdate (DocumentEvent e)
    {
        documentChanged();
    }

    // documentation inherited from interface DocumentListener
    public void insertUpdate (DocumentEvent e)
    {
        documentChanged();
    }

    // documentation inherited from interface DocumentListener
    public void removeUpdate (DocumentEvent e)
    {
        documentChanged();
    }
}
