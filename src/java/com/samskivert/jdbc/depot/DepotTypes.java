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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 * Maintains a record of the persistent classes brought into the context of the associated SQL,
 * i.e. any class associated with a concrete table that would appear in FROM or JOIN clauses or as
 * the target of an UPDATE or an INSERT or any other place where a table abbreviation could be
 * constructed.
 *
 * The main motivation for breaking this functionality out into its own class is to encapsulate the
 * operation that throws {@link PersistenceException} as separate from the operations that throw
 * {@link SQLException}. Once this class has been constructed, it may be used to create {@link
 * SQLBuilder} instances without any {@link PersistenceException} worries.
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
        PersistenceContext ctx, QueryClause... clauses)
        throws PersistenceException
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

    public String getTableName (Class<? extends PersistentRecord> cl)
    {
        return _classMap.get(cl).getTableName();
    }

    public String getTableAbbreviation (Class<? extends PersistentRecord> cl)
    {
        int ix = _classList.indexOf(cl);
        if (ix < 0) {
            throw new IllegalArgumentException("Unknown persistence class: " + cl);
        }
        return "T" + (ix+1);
    }

    public String getColumnName (Class<? extends PersistentRecord> cl, String field)
    {
        return getMarshaller(cl).getFieldMarshaller(field).getColumnName();
    }

    public DepotMarshaller getMarshaller (Class cl)
    {
        return _classMap.get(cl);
    }

    public void addClass (PersistenceContext ctx, Class <? extends PersistentRecord> type)
        throws PersistenceException
    {
        if (_classMap.containsKey(type)) {
            return;
        }
        _classList.add(type);
        _classMap.put(type, ctx.getMarshaller(type));
    }

    protected DepotTypes (PersistenceContext ctx, Set<Class<? extends PersistentRecord>> others)
        throws PersistenceException
    {
        for (Class<? extends PersistentRecord> c : others) {
            addClass(ctx, c);
        }
    }

    protected DepotTypes ()
    {
    }

    /** A list of referenced classes, used to generate table abbreviations. */
    protected List<Class<? extends PersistentRecord>> _classList =
        new ArrayList<Class<? extends PersistentRecord>>();

    /** Classes mapped to marshallers, used for table names and field lists. */
    protected Map<Class, DepotMarshaller> _classMap = new HashMap<Class, DepotMarshaller>();
}
