//
// $Id: Dumper.java,v 1.1 2001/07/14 00:51:13 mdb Exp $

package com.samskivert.viztool.util;

import java.lang.reflect.*;

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

        dump("F", clazz.getDeclaredFields());
        dump("C", clazz.getDeclaredConstructors());
        dump("M", clazz.getDeclaredMethods());
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
