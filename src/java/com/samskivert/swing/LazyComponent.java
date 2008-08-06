//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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
