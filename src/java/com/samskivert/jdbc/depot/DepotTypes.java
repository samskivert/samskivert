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

package com.samskivert.jdbc.depot;

import java.sql.SQLException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 * Maintains a record of the persistent classes brought into the context of the associated SQL,
 * i.e. any class associated with a concrete table that would appear in FROM or JOIN clauses or as
 * the target of an UPDATE or an INSERT or any other place where a table abbreviation could be
 * constructed.
 *
 * The main motivation for breaking this functionality out into its own class is to encapsulate the
 * operation that throws {@link DatabaseException} as separate from the operations that throw
 * {@link SQLException}. Once this class has been constructed, it may be used to create {@link
 * SQLBuilder} instances without any {@link DatabaseException} worries.
 */
public class DepotTypes
{
    /** A trivial instance that is accessible in places where we want the dialectal benefits of the
     * SQLBuilder without really requiring per-persistent-class context. */
    public static DepotTypes TRIVIAL = new DepotTypes();

    /**
     * Conveniently constructs a {@link DepotTypes} object given {@link QueryClause} objects, which
     * are interrogated for their class definition sets through {@link SQLExpression#addClasses}.
     */
    public static <T extends PersistentRecord> DepotTypes getDepotTypes (
        PersistenceContext ctx, Collection<? extends QueryClause> clauses)
        throws DatabaseException
    {
        Set<Class<? extends PersistentRecord>> classSet =
            new HashSet<Class<? extends PersistentRecord>>();
        for (QueryClause clause : clauses) {
            if (clause != null) {
                clause.addClasses(classSet);
            }
        }
        return new DepotTypes(ctx, classSet);
    }

    /**
     * A varargs version of {@link #getDepotTypes(PersistenceContext,Collection<QueryClause>)}.
     */
    public static <T extends PersistentRecord> DepotTypes getDepotTypes (
        PersistenceContext ctx, QueryClause... clauses)
        throws DatabaseException
    {
        return getDepotTypes(ctx, Arrays.asList(clauses));
    }

    /**
     * Create a new DepotTypes with the given {@link PersistenceContext} and a collection of
     * persistent record classes.
     */
    public DepotTypes (PersistenceContext ctx, Collection<Class<? extends PersistentRecord>> others)
        throws DatabaseException
    {
        for (Class<? extends PersistentRecord> c : others) {
            addClass(ctx, c);
        }
    }

    /**
     * Create a new DepotTypes with the given {@link PersistenceContext} and the given
     * persistent record.
     */
    public DepotTypes (PersistenceContext ctx, Class<? extends PersistentRecord> pClass)
        throws DatabaseException
    {
        addClass(ctx, pClass);
    }

    /**
     * Return the full table name of the given persistent class, which must have been previously
     * registered with this object.
     */
    public String getTableName (Class<? extends PersistentRecord> cl)
    {
        return getMarshaller(cl).getTableName();
    }

    /**
     * Return the current abbreviation by which we refer to the table associated with the given
     * persistent record -- which must have been previously registered with this object. If the
     * useTableAbbreviations flag is false, we return the full table name instead.
     *
     * @exception IllegalArgumentException thrown if the specified class is not known.
     */
    public String getTableAbbreviation (Class<? extends PersistentRecord> cl)
    {
        if (_useTableAbbreviations) {
            Integer ix = _classIx.get(cl);
            if (ix == null) {
                throw new IllegalArgumentException("Unknown persistence class: " + cl);
            }
            return "T" + (ix+1);
        }
        return getTableName(cl);
    }

    /**
     * Return the associated database column of the given field of the given persistent class,
     * throwing an exception if the record has not been registered with this object, or if the
     * field is unknown on the record.
     *
     * @exception IllegalArgumentException thrown if the specified field is not part of the
     * specified persistent class.
     */
    public String getColumnName (Class<? extends PersistentRecord> cl, String field)
    {
        FieldMarshaller<?> fm = getMarshaller(cl).getFieldMarshaller(field);
        if (fm == null) {
            throw new IllegalArgumentException(
                "Field not known on class [field=" + field + ", class=" + cl + "]");
        }
        return fm.getColumnName();
    }

    /**
     * Return the {@link DepotMarshaller} associated with the given persistent class, if it's been
     * registered with this object.
     *
     * @exception IllegalArgumentException thrown if the specified class is not known.
     */
    public DepotMarshaller<?> getMarshaller (Class<? extends PersistentRecord> cl)
    {
        DepotMarshaller<?> marsh = _classMap.get(cl);
        if (marsh == null) {
            throw new IllegalArgumentException("Persistent class not known: " + cl);
        }
        return marsh;
    }

    /**
     * Register a new persistent class with this object.
     */
    public void addClass (PersistenceContext ctx, Class <? extends PersistentRecord> type)
        throws DatabaseException
    {
        if (_classMap.containsKey(type)) {
            return;
        }

        // add the class in question
        DepotMarshaller<?> marsh = ctx.getMarshaller(type);
        _classMap.put(type, marsh);
        _classIx.put(type, _classIx.size());

        // if this class is @Computed and has a shadow, add its shadow
        if (marsh.getComputed() != null &&
            !PersistentRecord.class.equals(marsh.getComputed().shadowOf())) {
            addClass(ctx, marsh.getComputed().shadowOf());
        }
    }

    /**
     * Return the value of the useTableAbbreviations flag, which governs the behaviour when
     * referencing columns during SQL construction. Normally, this flag is on, and tables are
     * referenced as e.g. T1.itemId, but there are cases of weak/broken SQL where abbreviations
     * may not be brought into play. In these cases we prepend the full table name.
     */
    public boolean getUseTableAbbreviations ()
    {
        return _useTableAbbreviations;
    }

    /**
     * Sets the value of the useTableAbbreviations flag, which governs the behaviour when
     * referencing columns during SQL construction. Normally, this flag is on, and tables are
     * referenced as e.g. T1.itemId, but there are cases of weak/broken SQL where abbreviations
     * may not be brought into play. In these cases we prepend the full table name.
     */
    public void setUseTableAbbreviations (boolean doUse)
    {
        _useTableAbbreviations = doUse;
    }

    // constructor used to create TRIVIAL
    protected DepotTypes ()
    {
    }

    /** Classes mapped to integers, used for table abbreviation indexing. */
    protected Map<Class<?>, Integer> _classIx = new HashMap<Class<?>, Integer>();

    /** Classes mapped to marshallers, used for table names and field lists. */
    protected Map<Class<?>, DepotMarshaller<?>> _classMap =
        new HashMap<Class<?>, DepotMarshaller<?>>();

    /** When false, override the normal table abbreviations and return full table names instead. */
    protected boolean _useTableAbbreviations = true;
}
