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

package com.samskivert.swing.tests;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;

import com.samskivert.swing.RuntimeAdjust;
import com.samskivert.util.PrefsConfig;

/**
 * Does something extraordinary.
 */
public class AdjustTestApp
{
    public static void main (String[] args)
    {
        PrefsConfig config = new PrefsConfig("test");
        new RuntimeAdjust.IntAdjust(
            "This is a test adjustment. It is nice.",
            "samskivert.test.int_adjust1", config, 5);
        new RuntimeAdjust.IntAdjust(
            "This is another test adjustment. It is nice.",
            "samskivert.thwack.int_adjust2", config, 15);
        new RuntimeAdjust.BooleanAdjust(
            "This is a test adjustment. It is nice.",
            "samskivert.thwack.boolean_adjust1", config, true);

        new RuntimeAdjust.IntAdjust(
            "This is an other test adjustment. It is nice.",
            "otherpackage.test.int_adjust2", config, 15);
        new RuntimeAdjust.BooleanAdjust(
            "This is a an other test adjustment. It is nice.",
            "otherpackage.test.boolean_adjust1", config, false);
        new RuntimeAdjust.EnumAdjust(
            "This is yet an other test adjustment.",
            "otherpackage.test.enum_adjust1", config,
            new String[] { "debug", "info", "warning" }, "info");

        JFrame frame = new JFrame();
        ((JComponent)frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        frame.getContentPane().add(RuntimeAdjust.createAdjustEditor(), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.show();
    }
}
