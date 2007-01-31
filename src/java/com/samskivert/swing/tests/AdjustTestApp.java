//
// $Id: AdjustTestApp.java,v 1.2 2003/01/15 03:24:53 mdb Exp $

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
