//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.xml;

import java.lang.NoSuchFieldException;
import java.lang.reflect.Field;

import org.apache.commons.digester.Rule;

import com.samskivert.util.ValueMarshaller;

/**
 * Sets a field in the object on the top of the stack with a value parsed
 * from the body of an element.
 */
public class SetFieldRule extends Rule
{
    /**
     * Constructs a set field rule for the specified field.
     */
    public SetFieldRule (String fieldName)
    {
        // keep this for later
        _fieldName = fieldName;
    }

    @Override
    public void body (String namespace, String name, String bodyText)
        throws Exception
    {
        _bodyText = bodyText.trim();
    }

    @Override
    public void end (String namespace, String name)
        throws Exception
    {
        Object top = digester.peek();
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug(
                "  Setting '" + _fieldName + "' to '" +
                _bodyText + "' on '" + top + "'.");
        }

        // convert the source string into an object and set the field
        try {
            Field field = top.getClass().getField(_fieldName);
            Object value = ValueMarshaller.unmarshal(
                field.getType(), _bodyText);
            field.set(top, value);
        } catch (NoSuchFieldException nsfe) {
            digester.getLogger().warn(
                "No such field: " + top.getClass() + "." + _fieldName);
        }
    }

    protected String _fieldName;
    protected String _bodyText;
}
