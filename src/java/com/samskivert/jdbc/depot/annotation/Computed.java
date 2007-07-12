//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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

import com.samskivert.jdbc.depot.QueryBuilderContext;
import com.samskivert.jdbc.depot.PersistentRecord;

/**
 * Marks a field as computed, meaning it is ignored for schema purposes and it does not directly
 * correspond to a column in a table, thus its value must be overridden in the {@link
 * QueryBuilderContext}.
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface Computed
{
    /** If this value is false, the field is not populated at all. */
    boolean required () default true;

    /** A non-empty value here is taken as literal SQL and used to populate the computed field. */
    String fieldDefinition () default "";

    /**
     * A computed record can shadow a concrete record, which causes any field the former has in
     * common with the latter and which is not otherwise overriden to inherit its definition. The
     * shadowed class may also be given at the field level.
     *
     * The purpose of shadowing is largely to avoid having to supply a {@link FieldOverride} when
     * querying for objects that that contain large subsets of some other persistent object's
     * fields -- in other words, when you use a computed entity to query only some of the columns
     * from a table.
     * 
     * The shadowed class must have been brought into the query using e.g. {@link FromOverride}
     * or {@link Join} clauses. The referenced fields must be simple concrete columns in a table;
     * they must themselves be computed or overridden or shadowing.
     * 
     * TODO: Do in fact let the shadowed field be computed, overriden or shadowing.
     */
    Class<? extends PersistentRecord> shadowOf () default PersistentRecord.class;
}
