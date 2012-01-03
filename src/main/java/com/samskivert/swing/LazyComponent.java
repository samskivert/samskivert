//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;

/**
 * A component that doesn't actually create its content until it is
 * visible and actually showing. This was made to support lazy-creation
 * in JTabbedPane, since normally adding a compent to a JTabbedPane
 * creates each component and their associated controllers.
 */
public class LazyComponent extends JComponent
{
    /**
     * An interface for creating the actual content that will live
     * in this component.
     */
    public interface ContentCreator
    {
        /**
         * Create the content at the time that it is needed.
         */
        public JComponent createContent ();
    }

    /**
     * Create a lazy component.
     */
    public LazyComponent (ContentCreator creator)
    {
        _creator = creator;
    }

    @Override
    public void addNotify ()
    {
        super.addNotify();

        checkCreate();
    }

    @Override
    public void setVisible (boolean vis)
    {
        super.setVisible(vis);

        checkCreate();
    }

    /**
     * Check to see if we should now create the content.
     */
    protected void checkCreate ()
    {
        if (_creator != null && isShowing()) {
            // ideally, we would replace ourselves in our parent, but that
            // doesn't seem to work in JTabbedPane
            setLayout(new BorderLayout());
            add(_creator.createContent(), BorderLayout.CENTER);
            _creator = null;
        }
    }

    /** The content creator. */
    protected ContentCreator _creator;
}
