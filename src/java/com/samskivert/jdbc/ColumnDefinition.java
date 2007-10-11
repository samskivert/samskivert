//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006-2007 Michael Bayne, Pär Winzell
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

package com.samskivert.jdbc;

/**
 * An object representing the configuration of a typical SQL column.
 */
public class ColumnDefinition
{
    public ColumnDefinition (String type)
    {
        this(type, false, false, null);
    }

    public ColumnDefinition (
        String type, boolean nullable, boolean unique, String defaultValue)
    {
        if (type == null) {
            throw new IllegalArgumentException("cannot build column definition without type");
        }

        _type = type;
        _nullable = nullable;
        _unique = unique;
        _defaultValue = defaultValue;
    }

    /** Returns the SQL type of this column, e.g. INTEGER. */
    public String getType ()
    {
        return _type;
    }

    /** Determines whether or not this column should allow NULL values. */
    public boolean isNullable ()
    {
        return _nullable;
    }

    /** Determines whether or not this column should reject duplicate values. */
    public boolean isUnique ()
    {
        return _unique;
    }

    /** Returns the value to insert into this column instead of nulls, if any. */
    public String getDefaultValue ()
    {
        return _defaultValue;
    }

    protected String _type;
    protected boolean _nullable;
    protected boolean _unique;
    protected String _defaultValue;
}
