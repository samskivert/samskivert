//
// $Id: Dumper.java,v 1.4 2001/08/14 06:23:08 mdb Exp $
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

package com.samskivert.viztool.util;

import java.lang.reflect.*;
import com.samskivert.util.StringUtil;

/**
 * A simple utility that dumps out information available via reflection.
 */
public class Dumper
{
    public void dump (Class clazz)
    {
        System.out.println("Dumping: " + clazz.getName());

        Class parent = clazz.getSuperclass();
        if (parent == null) {
            System.out.println("P: none");
        } else {
            System.out.println("P: " + parent.getName());
        }

        Class[] ifaces = clazz.getInterfaces();
        for (int i = 0; i < ifaces.length; i++) {
            System.out.println("I: " +  ifaces[i].getName());
        }

        dumpFields("F", clazz.getDeclaredFields());
        dump("C", clazz.getDeclaredConstructors());
        dumpMethods("M", clazz.getDeclaredMethods());
    }

    protected void dumpFields (String prefix, Field[] fields)
    {
        for (int i = 0; i < fields.length; i++) {
            System.out.println(prefix + ": " + fields[i].getName() +
                               " / " + fields[i].getType().getName());
        }
    }

    protected void dumpMethods (String prefix, Method[] methods)
    {
        for (int i = 0; i < methods.length; i++) {
            System.out.println(prefix + ": " + methods[i].getName() +
                               StringUtil.toString(
                                   methods[i].getParameterTypes()));
        }
    }

    protected void dump (String prefix, Member[] members)
    {
        for (int i = 0; i < members.length; i++) {
            System.out.println(prefix + ": " + members[i].getName());
        }
    }

    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: Dumper classname [classname ...]");
            System.exit(-1);
        }

        String classpath = System.getProperty("java.class.path", ".");
        System.out.println("Classpath: " + classpath);

        Dumper dumper = new Dumper();

        for (int i = 0; i < args.length; i++) {
            try {
                dumper.dump(Class.forName(args[i]));

            } catch (Exception e) {
                System.err.println("Unable to instantiate class: " + args[i]);
                e.printStackTrace();
            }
        }
    }
}
