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

package com.samskivert.jdbc.depot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines a primary key generator that may be referenced by name when a generator
 * element is specified for the GeneratedValue annotation. A table generator may be specified on
 * the entity class or on the primary key field. The scope of the generator name is global to the
 * persistence unit (across all generator types).
 */
@Target(value={ElementType.TYPE, ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface TableGenerator
{
    /**
     * A unique generator name that can be referenced by one or more classes to be the
     * generator for id values.
     */
    String name ();
    
    /**
     * Name of table that stores the generated id values. Defaults to a name chosen by persistence
     * provider.
     */
    String table () default "";

    /**
     * Name of the primary key column in the table Defaults to a provider-chosen name.
     */
    String pkColumnName () default "";

    /**
     * Name of the column that stores the last value generated Defaults to a provider-chosen name.
     */    
    String valueColumnName () default "";

    /**
     * The primary key value in the generator table that distinguishes this set of generated values
     * from others that may be stored in the table Defaults to a provider-chosen value to store in
     * the primary key column of the generator table
     */    
    String pkColumnValue () default "";
}
