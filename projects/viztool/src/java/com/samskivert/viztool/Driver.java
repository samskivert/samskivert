//
// $Id: Driver.java,v 1.15 2001/12/03 08:34:53 mdb Exp $
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

package com.samskivert.viztool;

import java.awt.*;
import java.awt.print.*;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.viztool.enum.*;

import com.samskivert.viztool.hierarchy.HierarchyVisualizer;
import com.samskivert.viztool.summary.SummaryVisualizer;
import com.samskivert.viztool.util.FontPicker;

/**
 * The application driver. This class parses the command line arguments
 * and invokes the visualization code.
 */
public class Driver
{
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println(USAGE);
            System.exit(-1);
        }

        // parse our arguments
        String pkgroot = "";
        String regexp = null;
        boolean print = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-print")) {
                print = true;
            } else if (regexp == null) {
                regexp = args[i];
            }
        }

        // run ourselves on the classpath
        String classpath = System.getProperty("java.class.path");
        // System.err.println("Scanning " + classpath + ".");
        ClassEnumerator enum = new ClassEnumerator(classpath);

        // print out the warnings
        ClassEnumerator.Warning[] warnings = enum.getWarnings();
        for (int i = 0; i < warnings.length; i++) {
            System.err.println("Warning: " + warnings[i].reason);
        }

        // initialize the font picker
        FontPicker.init(print);

        // and finally generate the visualization
        FilterEnumerator fenum = null;
        try {
            fenum = new RegexpEnumerator(regexp, enum);
        } catch  (Exception e) {
            Log.warning("Invalid package regular expression " +
                        "[regexp=" + regexp + ", error=" + e + "].");
            System.exit(-1);
        }

        // Visualizer viz = new HierarchyVisualizer(pkgroot, penum);
        Visualizer viz = new SummaryVisualizer(pkgroot, fenum);

        if (print) {
            // we use the print system to render things
            PrinterJob job = PrinterJob.getPrinterJob();

            // pop up a dialog to format our pages
            // PageFormat format = job.pageDialog(job.defaultPage());
            PageFormat format = job.defaultPage();

            // use sensible margins
            Paper paper = new Paper();
            paper.setImageableArea(72*0.5, 72*0.5, 72*7.5, 72*10);
            format.setPaper(paper);

            // use our configured page format
            job.setPrintable(viz, format);

            // pop up a dialog to control printing
            if (job.printDialog()) {
                try {
                    // invoke the printing process
                    job.print();
                } catch (PrinterException pe) {
                    pe.printStackTrace(System.err);
                }

            } else {
                Log.info("Printing cancelled.");
            }

            // printing starts up the AWT threads, so we have to
            // explicitly exit at this point
            System.exit(0);

        } else {
            VizFrame frame = new VizFrame(viz);
            frame.pack();
            SwingUtil.centerWindow(frame);
            frame.setVisible(true);
        }
    }

    protected static final String USAGE =
        "Usage: Driver [-mode hier|sum] [-print] package_regexp " +
        "[package_root]\n" +
        "       hier = class hierarchy visualization\n" +
        "       sum = class summary visualization\n"
        ;
}
