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
 * Specifies the primary field(s) of an entity.
 */
@Target(value=ElementType.TYPE)
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Entity
{
    /** The name of an entity. Defaults to the unqualified name of the entity class. */
    String name () default "";

    /** Unique constraints that are to be placed on this entity's table. These constraints apply in
     * addition to any constraints specified by the Column annotation and constraints entailed by
     * primary key mappings. Defaults to no additional constraints. */
    UniqueConstraint[] uniqueConstraints () default {};

    /** Indices to add to this entity's table. */
    Index[] indices () default {};

    /** Full-text search indexes defined on this entity, if any. Defaults to none. */
    FullTextIndex[] fullTextIndices () default {};
}
