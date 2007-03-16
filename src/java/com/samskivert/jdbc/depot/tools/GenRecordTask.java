//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2007 Michael Bayne
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

package com.samskivert.jdbc.depot.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.util.GenUtil;
import com.samskivert.util.StringUtil;
import com.samskivert.velocity.VelocityUtil;

/**
 * An ant task that updates the column constants for a persistent record.
 */
public class GenRecordTask extends Task
{
    /**
     * Adds a nested fileset element which enumerates record source files.
     */
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    /**
     * Configures that classpath that we'll use to load record classes.
     */
    public void setClasspathref (Reference pathref)
    {
        _cloader = ClasspathUtils.getClassLoaderForPath(getProject(), pathref);
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        if (_cloader == null) {
            String errmsg = "This task requires a 'classpathref' attribute " +
                "to be set to the project's classpath.";
            throw new BuildException(errmsg);
        }

        try {
            _velocity = VelocityUtil.createEngine();
        } catch (Exception e) {
            throw new BuildException("Failure initializing Velocity", e);
        }

        // resolve the PersistentRecord class using our classloader
        try {
            _prclass = _cloader.loadClass(PersistentRecord.class.getName());
        } catch (Exception e) {
            throw new BuildException("Can't resolve InvocationListener", e);
        }

        for (FileSet fs : _filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (int f = 0; f < srcFiles.length; f++) {
                processRecord(new File(fromDir, srcFiles[f]));
            }
        }
    }

    /**
     * Processes a distributed object source file.
     */
    protected void processRecord (File source)
    {
        // System.err.println("Processing " + source + "...");

        // load up the file and determine it's package and classname
        String name = null;
        try {
            name = readClassName(source);
        } catch (Exception e) {
            System.err.println("Failed to parse " + source + ": " + e.getMessage());
        }

        try {
            processRecord(source, _cloader.loadClass(name));
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Failed to load " + name + ".\n" +
                               "Missing class: " + cnfe.getMessage());
            System.err.println("Be sure to set the 'classpathref' attribute to a classpath\n" +
                               "that contains your projects invocation service classes.");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /** Processes a resolved distributed object class instance. */
    protected void processRecord (File source, Class oclass)
    {
        // make sure we extend persistent record
        if (!_prclass.isAssignableFrom(oclass)) {
            // System.err.println("Skipping " + oclass.getName() + "...");
            return;
        }

        // determine which fields we need to deal with and those that make up our primary key
        List<Field> flist = new ArrayList<Field>(), kflist = new ArrayList<Field>();
        Field[] fields = oclass.getDeclaredFields();
        for (int ii = 0; ii < fields.length; ii++) {
            Field f = fields[ii];
            int mods = f.getModifiers();
            if (!Modifier.isPublic(mods) || Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                continue;
            }
            boolean found = false;
            // iterate becase getAnnotation() fails if we're dealing with multiple classloaders
            for (Annotation a : f.getDeclaredAnnotations()) {
                if (Id.class.getName().equals(a.annotationType().getName())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                kflist.add(f);
            }
            flist.add(f);
        }

        // slurp our source file into newline separated strings
        String[] lines = null;
        try {
            BufferedReader bin = new BufferedReader(new FileReader(source));
            ArrayList<String> llist = new ArrayList<String>();
            String line = null;
            while ((line = bin.readLine()) != null) {
                llist.add(line);
            }
            lines = llist.toArray(new String[llist.size()]);
            bin.close();
        } catch (IOException ioe) {
            System.err.println("Error reading '" + source + "': " + ioe);
            return;
        }

        // now determine where to insert our static field declarations
        int bstart = -1, bend = -1;
        int nstart = -1, nend = -1;
        int mstart = -1, mend = -1;
        for (int ii = 0; ii < lines.length; ii++) {
            String line = lines[ii].trim();

            // look for the start of the class body
            if (NAME_PATTERN.matcher(line).find()) {
                if (line.endsWith("{")) {
                    bstart = ii+1;
                } else {
                    // search down a few lines for the open brace
                    for (int oo = 1; oo < 10; oo++) {
                        if (get(lines, ii+oo).trim().endsWith("{")) {
                            bstart = ii+oo+1;
                            break;
                        }
                    }
                }

            // track the last } on a line by itself and we'll call that the end of the class body
            } else if (line.equals("}")) {
                bend = ii;

            // look for our field and method markers
            } else if (line.equals(FIELDS_START)) {
                nstart = ii;
            } else if (line.equals(FIELDS_END)) {
                nend = ii+1;
            } else if (line.equals(METHODS_START)) {
                mstart = ii;
            } else if (line.equals(METHODS_END)) {
                mend = ii+1;
            }
        }

        // sanity check the markers
        if (check(source, "fields start", nstart, "fields end", nend) ||
            check(source, "fields end", nend, "fields start", nstart) ||
            check(source, "methods start", mstart, "methods end", mend) ||
            check(source, "methods end", mend, "methods start", mstart)) {
            return;
        }

        // we have no previous markers then stuff the fields at the top of the class body and the
        // methods at the bottom
        if (nstart == -1) {
            nstart = bstart;
            nend = bstart;
        }
        if (mstart == -1) {
            mstart = bend;
            mend = bend;
        }

        // get the unqualified class name
        String rname = oclass.getName();
        rname = rname.substring(rname.lastIndexOf(".")+1);

        // generate our fields section
        StringBuilder fsection = new StringBuilder();
        for (int ii = 0; ii < flist.size(); ii++) {
            Field f = flist.get(ii);
            String fname = f.getName();

            // create our velocity context
            VelocityContext ctx = new VelocityContext();
            ctx.put("record", rname);
            ctx.put("field", fname);
            ctx.put("capfield", StringUtil.unStudlyName(fname).toUpperCase());

            // now generate our bits
            StringWriter fwriter = new StringWriter();
            try {
                _velocity.mergeTemplate(NAME_TMPL, "UTF-8", ctx, fwriter);
            } catch (Exception e) {
                System.err.println("Failed processing template");
                e.printStackTrace(System.err);
            }

            // and append them as appropriate to the string buffers
            if (ii > 0) {
                fsection.append("\n");
            }
            fsection.append(fwriter.toString());
        }

        // generate our methods section
        StringBuilder msection = new StringBuilder();

        // add a getKey() method, if applicable
        if (kflist.size() > 0) {
            // create our velocity context
            VelocityContext ctx = new VelocityContext();
            ctx.put("record", rname);

            StringBuilder argList = new StringBuilder();
            StringBuilder argNameList = new StringBuilder();
            StringBuilder fieldNameList = new StringBuilder();
            for (Field keyField : kflist) {
                if (argList.length() > 0) {
                    argList.append(", ");
                    argNameList.append(", ");
                    fieldNameList.append(", ");
                }
                String name = keyField.getName();
                argList.append(GenUtil.simpleName(keyField)).append(" ").append(name);
                argNameList.append(name);
                fieldNameList.append(StringUtil.unStudlyName(name));
            }

            ctx.put("argList", argList.toString());
            ctx.put("argNameList", argNameList.toString());
            ctx.put("fieldNameList", fieldNameList.toString());

            // now generate our bits
            StringWriter mwriter = new StringWriter();
            try {
                _velocity.mergeTemplate(KEY_TMPL, "UTF-8", ctx, mwriter);
            } catch (Exception e) {
                System.err.println("Failed processing template");
                e.printStackTrace(System.err);
            }

            // and append them as appropriate to the string buffers
            msection.append(mwriter.toString());
        }

        // now bolt everything back together into a class declaration
        try {
            BufferedWriter bout = new BufferedWriter(new FileWriter(source));
            for (int ii = 0; ii < nstart; ii++) {
                writeln(bout, lines[ii]);
            }

            if (fsection.length() > 0) {
                String prev = get(lines, nstart-1);
                if (!StringUtil.isBlank(prev) && !prev.equals("{")) {
                    bout.newLine();
                }
                writeln(bout, "    " + FIELDS_START);
                bout.write(fsection.toString());
                writeln(bout, "    " + FIELDS_END);
                if (!StringUtil.isBlank(get(lines, nend))) {
                    bout.newLine();
                }
            }
            for (int ii = nend; ii < mstart; ii++) {
                writeln(bout, lines[ii]);
            }

            if (msection.length() > 0) {
                if (!StringUtil.isBlank(get(lines, mstart-1))) {
                    bout.newLine();
                }
                writeln(bout, "    " + METHODS_START);
                bout.write(msection.toString());
                writeln(bout, "    " + METHODS_END);
                String next = get(lines, mend);
                if (!StringUtil.isBlank(next) && !next.equals("}")) {
                    bout.newLine();
                }
            }
            for (int ii = mend; ii < lines.length; ii++) {
                writeln(bout, lines[ii]);
            }

            bout.close();
        } catch (IOException ioe) {
            System.err.println("Error writing to '" + source + "': " + ioe);
        }
    }

    /** Safely gets the <code>index</code>th line, returning the empty string if we exceed the
     * length of the array. */
    protected String get (String[] lines, int index)
    {
        return (index < lines.length) ? lines[index] : "";
    }

    /** Helper function for sanity checking marker existence. */
    protected boolean check (File source, String mname, int mline, String fname, int fline)
    {
        if (mline == -1 && fline != -1) {
            System.err.println("Found " + fname + " marker (at line " + (fline+1) + ") but no " +
                               mname + " marker in '" + source + "'.");
            return true;
        }
        return false;
    }

    /** Helper function for writing a string and a newline to a writer. */
    protected void writeln (BufferedWriter bout, String line)
        throws IOException
    {
        bout.write(line);
        bout.newLine();
    }

    /**
     * Reads in the supplied source file and locates the package and class or interface name and
     * returns a fully qualified class name.
     */
    protected static String readClassName (File source)
        throws IOException
    {
        // load up the file and determine it's package and classname
        String pkgname = null, name = null;
        BufferedReader bin = new BufferedReader(new FileReader(source));
        String line;
        while ((line = bin.readLine()) != null) {
            Matcher pm = PACKAGE_PATTERN.matcher(line);
            if (pm.find()) {
                pkgname = pm.group(1);
            }
            Matcher nm = NAME_PATTERN.matcher(line);
            if (nm.find()) {
                name = nm.group(2);
                break;
            }
        }
        bin.close();

        // make sure we found something
        if (name == null) {
            throw new IOException("Unable to locate class or interface name in " + source + ".");
        }

        // prepend the package name to get a name we can Class.forName()
        if (pkgname != null) {
            name = pkgname + "." + name;
        }

        return name;
    }

    /** A list of filesets that contain tile images. */
    protected ArrayList<FileSet> _filesets = new ArrayList<FileSet>();

    /** Used to do our own classpath business. */
    protected ClassLoader _cloader;

    /** Used to generate source files from templates. */
    protected VelocityEngine _velocity;

    /** {@link PersistentRecord} resolved with the proper classloader so that we can compare it to
     * loaded derived classes. */
    protected Class<?> _prclass;

    /** Specifies the path to the name code template. */
    protected static final String NAME_TMPL = "com/samskivert/jdbc/depot/tools/record_name.tmpl";

    /** Specifies the path to the key code template. */
    protected static final String KEY_TMPL = "com/samskivert/jdbc/depot/tools/record_key.tmpl";

    // markers
    protected static final String MARKER = "// AUTO-GENERATED: ";
    protected static final String FIELDS_START = MARKER + "FIELDS START";
    protected static final String FIELDS_END = MARKER + "FIELDS END";
    protected static final String METHODS_START = MARKER + "METHODS START";
    protected static final String METHODS_END = MARKER + "METHODS END";

    /** A regular expression for matching the package declaration. */
    protected static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+(\\S+)\\W");

    /** A regular expression for matching the class or interface declaration. */
    protected static final Pattern NAME_PATTERN = Pattern.compile(
        "^\\s*public\\s+(?:abstract\\s+)?(interface|class)\\s+(\\w+)(\\W|$)");
}
