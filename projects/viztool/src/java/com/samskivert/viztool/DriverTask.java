//
// $Id: DriverTask.java,v 1.2 2001/12/03 08:53:45 mdb Exp $
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

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

import com.samskivert.swing.util.SwingUtil;

import com.samskivert.viztool.enum.ClassEnumerator;
import com.samskivert.viztool.enum.FilterEnumerator;
import com.samskivert.viztool.enum.RegexpEnumerator;
import com.samskivert.viztool.util.FontPicker;

/**
 * The viztool ant task. It takes the following arguments:
 *
 * <pre>
 * pkgroot = the base package from which names will be shortened
 * classes = a regular expression matching the classes to be visualized
 * visualizer = the classname of the visualizer to be used
 * </pre>
 *
 * The task should contain an embedded &lt;classpath&gt; element to
 * provide the classpath over which we will iterate, looking for matching
 * classes.
 */
public class DriverTask extends Task
{
    public void setVisualizer (String vizclass)
    {
        _vizclass = vizclass;
    }

    public void setPkgroot (String pkgroot)
    {
        _pkgroot = pkgroot;
    }

    public void setClasses (String classes)
    {
        _classes = classes;
    }

    public void setOutput (File output)
    {
        _output = output;
    }

    public Path createClasspath ()
    {
        return _cmdline.createClasspath(project).createPath();
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        // make sure everything was set up properly
        ensureSet(_vizclass, "Must specify the visualizer class " +
                  "via the 'visualizer' attribute.");
        ensureSet(_pkgroot, "Must specify the package root " +
                  "via the 'pkgroot' attribute.");
        ensureSet(_pkgroot, "Must specify the class regular expression " +
                  "via the 'classes' attribute.");
        Path classpath = _cmdline.getClasspath();
        ensureSet(classpath, "Must provide a <classpath> subelement " +
                  "describing the classpath to be searched for classes.");

        // initialize the font picker
        FontPicker.init(_output != null);

        // create the classloader we'll use to load the visualized classes
        ClassLoader cl = new AntClassLoader(null, project, classpath, false);

        // scan the classpath and determine which classes will be
        // visualized
        ClassEnumerator enum = new ClassEnumerator(classpath.toString());
        FilterEnumerator fenum = null;
        try {
            fenum = new RegexpEnumerator(_classes, enum);
        } catch  (Exception e) {
            throw new BuildException("Invalid package regular expression " +
                                     "[classes=" + _classes + "].", e);
        }

        ArrayList classes = new ArrayList();
        while (fenum.hasNext()) {
            String cname = (String)fenum.next();
            // skip inner classes, the visualizations pick those up
            // themselves
            if (cname.indexOf("$") != -1) {
                continue;
            }
            try {
                classes.add(cl.loadClass(cname));
            } catch (Throwable t) {
                log("Unable to introspect class [class=" + cname +
                    ", error=" + t + "].");
            }
        }

//         // remove the packages on our exclusion list
//         String expkg = System.getProperty("exclude");
//         if (expkg != null) {
//             StringTokenizer tok = new StringTokenizer(expkg, ":");
//             while (tok.hasMoreTokens()) {
//                 pkgset.remove(tok.nextToken());
//             }
//         }

        // now create our visualizer and go to work
        Visualizer viz = null;
        try {
            viz = (Visualizer)Class.forName(_vizclass).newInstance();
        } catch (Throwable t) {
            throw new BuildException("Unable to instantiate visualizer " +
                                     "[vizclass=" + _vizclass +
                                     ", error=" + t + "].");
        }

        viz.setPackageRoot(_pkgroot);
        viz.setClasses(classes.iterator());

        // if no output file was specified, pop up a window
        if (_output == null) {
            VizFrame frame = new VizFrame(viz);
            frame.pack();
            SwingUtil.centerWindow(frame);
            frame.setVisible(true);

            // prevent ant from kicking the JVM out from under us
            synchronized (this) {
                while (true) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }

        } else {
            // we use the print system to render things
            PrinterJob job = PrinterJob.getPrinterJob();

            // use sensible margins
            PageFormat format = job.defaultPage();
            Paper paper = new Paper();
            paper.setImageableArea(72*0.5, 72*0.5, 72*7.5, 72*10);
            format.setPaper(paper);

            // use our configured page format
            job.setPrintable(viz, format);

            // tell our printjob to print to a file
            PrintRequestAttributeSet attrs =
                new HashPrintRequestAttributeSet();
            String outpath = _output.getPath();
            try {
                URI target = new URI("file:" + outpath);
                attrs.add(new Destination(target));

            } catch (URISyntaxException use) {
                String errmsg = "Can't create URI for 'output' file path? " +
                    "[output=" + outpath + "].";
                throw new BuildException(errmsg, use);
            }

            // invoke the printing process
            try {
                log("Generating visualization to '" + outpath + "'.");
                job.print(attrs);
            } catch (PrinterException pe) {
                throw new BuildException("Error printing visualization.", pe);
            }
        }
    }

    protected void ensureSet (Object value, String errmsg)
        throws BuildException
    {
        if (value == null) {
            throw new BuildException(errmsg);
        }
    }

    protected String _vizclass;
    protected String _pkgroot;
    protected String _classes;
    protected File _output;

    // use use this for accumulating our classpath
    protected CommandlineJava _cmdline = new CommandlineJava();
}
