//
// $Id: DriverTask.java,v 1.1 2001/12/03 08:34:53 mdb Exp $
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

import java.io.File;
import java.io.IOException;

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

    public Path createClasspath ()
    {
        return _cmdline.createClasspath(project).createPath();
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        // initialize the font picker
        FontPicker.init(false);

        // create the classloader we'll use to load the visualized classes
        Path classpath = _cmdline.getClasspath();
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
    }

    protected String _vizclass;
    protected String _pkgroot;
    protected String _classes;

    // use use this for accumulating our classpath
    protected CommandlineJava _cmdline = new CommandlineJava();
}
