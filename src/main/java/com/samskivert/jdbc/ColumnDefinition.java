//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

/**
 * An object representing the configuration of a typical SQL column.
 */
public class ColumnDefinition
{
    public String type;
    public boolean nullable;
    public boolean unique;
    public String defaultValue;

    public ColumnDefinition ()
    {
        this(null);
    }

    public ColumnDefinition (String type)
    {
        this(type, false, false, null);
    }

    public ColumnDefinition (
        String type, boolean nullable, boolean unique, String defaultValue)
    {
        this.type = type;
        this.nullable = nullable;
        this.unique = unique;
        this.defaultValue = defaultValue;
    }
}
