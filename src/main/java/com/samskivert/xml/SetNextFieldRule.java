//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.xml;

import java.lang.reflect.Field;

import org.apache.commons.digester.Rule;

/**
 * Like the <code>SetNextRule</code> except that the object on the top of
 * the stack is placed into a field of the penultimate object.
 */
public class SetNextFieldRule extends Rule
{
    /**
     * Constructs a set field rule for the specified field.
     */
    public SetNextFieldRule (String fieldName)
    {
        // keep this for later
        _fieldName = fieldName;
    }

    @Override
    public void end (String namespace, String name)
        throws Exception
    {
        // identify the objects to be used
        Object child = digester.peek(0);
        Object parent = digester.peek(1);
        Class<?> pclass = parent.getClass();

        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("Set " + pclass.getName() + "." +
                                       _fieldName + " = " + child);
        }

        // stuff the child object into the field of the parent
        Field field = pclass.getField(_fieldName);
        field.set(parent, child);
    }

    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder("SetNextFieldRule[");
        sb.append("fieldName=");
        sb.append(_fieldName);
        sb.append("]");
        return (sb.toString());
    }

    protected String _fieldName;
}
