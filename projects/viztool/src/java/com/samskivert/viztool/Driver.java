//
// $Id: Driver.java,v 1.3 2001/07/17 05:21:33 mdb Exp $

package com.samskivert.viztool;

import java.awt.*;
import java.awt.print.*;

import com.samskivert.viztool.enum.*;
import com.samskivert.viztool.viz.*;

public class Driver
{
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: Driver [-print] package_root");
            System.exit(-1);
        }

        // parse our arguments
        String pkgroot = null;
        boolean print = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-print")) {
                print = true;
            } else if (pkgroot == null) {
                pkgroot = args[i];
            }
        }

        // run ourselves on the classpath
        String classpath = System.getProperty("java.class.path");
        ClassEnumerator enum = new ClassEnumerator(classpath);

        // print out the warnings
        ClassEnumerator.Warning[] warnings = enum.getWarnings();
        for (int i = 0; i < warnings.length; i++) {
            System.err.println("Warning: " + warnings[i].reason);
        }

        // and finally generate the visualization
        PackageEnumerator penum = new PackageEnumerator(pkgroot, enum, true);
        HierarchyVisualizer viz = new HierarchyVisualizer(pkgroot, penum);

        if (print) {
            // we use the print system to render things
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(viz);

            // pop up a dialog to format our pages
            // PageFormat format = job.pageDialog(job.defaultPage());
            PageFormat format = job.defaultPage();

            // pop up a dialog to control printing
            job.printDialog();

            try {
                job.print();
            } catch (PrinterException pe) {
                pe.printStackTrace(System.err);
            }

            // printing starts up the AWT threads, so we have to
            // explicitly exit at this point
            System.exit(0);

        } else {
            TestFrame frame = new TestFrame(viz);

            // center the frame in the screen and show it
            Toolkit tk = frame.getToolkit();
            Dimension ss = tk.getScreenSize();
            int width = 640, height = 480;
            frame.setBounds((ss.width-width)/2, (ss.height-height)/2,
                            width, height);
            frame.setVisible(true);
        }
    }
}
