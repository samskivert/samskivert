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

package com.samskivert.jdbc.depot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.depot.annotation.Computed;
import com.samskivert.jdbc.depot.clause.FieldOverride;
import com.samskivert.jdbc.depot.clause.ForUpdate;
import com.samskivert.jdbc.depot.clause.FromOverride;
import com.samskivert.jdbc.depot.clause.GroupBy;
import com.samskivert.jdbc.depot.clause.Join;
import com.samskivert.jdbc.depot.clause.Limit;
import com.samskivert.jdbc.depot.clause.OrderBy;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.clause.Where;

/**
 * Builds actual SQL given a main persistent type and some {@link QueryClause} objects.
 */
public class SQLQueryBuilder<T>
{
    /** The from override clause, if any. */
    public FromOverride fromOverride;

    /** The where clause. */
    public Where where;

    /** A list of join clauses, each potentially referencing a new class. */
    public List<Join> joinClauses = new ArrayList<Join>();

    /** The order by clause, if any. */
    public OrderBy orderBy;

    /** The group by clause, if any. */
    public GroupBy groupBy;

    /** The limit clause, if any. */
    public Limit limit;

    /** The For Update clause, if any. */
    public ForUpdate forUpdate;

    /**
     * Constructs a {@link DepotTypes} object for your convenience given similar arguments used to
     * construct a {@link SQLQueryBuilder}. This method mainly offloads the task of interrogating
     * the various clauses for what persistent classes they introduce into the query.
     */
    public static <T> DepotTypes<T> getDepotTypes (
        PersistenceContext ctx, Class<? extends PersistentRecord> type, QueryClause... clauses)
        throws PersistenceException
    {
        Set<Class<? extends PersistentRecord>> classSet =
            new HashSet<Class<? extends PersistentRecord>>();
        if (type != null) {
            classSet.add(type);
        }

        for (QueryClause clause : clauses) {
            if (clause != null) {
                clause.addClasses(classSet);
            }
        }

        return new DepotTypes<T>(ctx, type, classSet);
    }

    /**
     * Creates a new Query object to generate one or more instances of the specified persistent
     * class, as dictated by the key and query clauses.  A persistence context is supplied for
     * instantiation of marshallers, which may trigger table creations and schema migrations.
     */
    public SQLQueryBuilder (PersistenceContext ctx, DepotTypes<T> types, String[] fields,
                            QueryClause... clauses)
    {
        _builder = new StringBuilder("select ");
        _types = types;

        // iterate over the clauses and sort them into the different types we understand
        for (QueryClause clause : clauses) {
            if (clause == null) {
                continue;
            }
            if (clause instanceof Where) {
                if (where != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple Where clauses.");
                }
                where = (Where) clause;

            } else if (clause instanceof FromOverride) {
                if (fromOverride != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple FromOverride clauses.");
                }
                fromOverride = (FromOverride) clause;

            } else if (clause instanceof Join) {
                joinClauses.add((Join) clause);

            } else if (clause instanceof FieldOverride) {
                _disMap.put(((FieldOverride) clause).getField(),
                            ((FieldOverride) clause));

            } else if (clause instanceof OrderBy) {
                if (orderBy != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple OrderBy clauses.");
                }
                orderBy = (OrderBy) clause;

            } else if (clause instanceof GroupBy) {
                if (groupBy != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple GroupBy clauses.");
                }
                groupBy = (GroupBy) clause;

            } else if (clause instanceof Limit) {
                if (limit != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple Limit clauses.");
                }
                limit = (Limit) clause;

            } else if (clause instanceof ForUpdate) {
                if (forUpdate != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple For Update clauses.");
                }
                forUpdate = (ForUpdate) clause;
            }
        }

        Computed entityComputed = _types.getMainType().getAnnotation(Computed.class);

        // iterate over the fields we're filling in and figure out whence each one comes
        boolean skip = true;
        for (String field : fields) {
            if (!skip) {
                _builder.append(", ");
            }
            skip = false;

            // first, see if there's a field override
            FieldOverride clause1 = _disMap.get(field);
            if (clause1 != null) {
                clause1.appendClause(_types, _builder);
                continue;
            }

            // figure out the class we're selecting from unless we're otherwise overriden:
            // for a concrete record, simply use the corresponding table; for a computed one,
            // default to the shadowed concrete record, or null if there isn't one
            Class<? extends PersistentRecord> tableClass =
                entityComputed == null ? _types.getMainType() : entityComputed.shadowOf();

            // handle the field-level @Computed annotation, if there is one
            Computed fieldComputed =
                _types.getMainMarshaller().getFieldMarshaller(field).getComputed();
            if (fieldComputed != null) {
                // check if the computed field has a literal SQL definition
                if (fieldComputed.fieldDefinition().length() > 0) {
                    _builder.append(fieldComputed.fieldDefinition()).append(" as ").append(field);
                    continue;
                }

                // or if we can simply ignore the field
                if (!fieldComputed.required()) {
                    skip = true;
                    continue;
                }

                // else see if there's an overriding shadowOf definition
                if (fieldComputed.shadowOf() != null) {
                    tableClass = fieldComputed.shadowOf();
                }
            }

            // if we get this far we hopefully have a table to select from
            if (tableClass != null) {
                String tableName = _types.getTableAbbreviation(tableClass);
                _builder.append(tableName).append(".").append(field);
                continue;
            }

            // else owie
            throw new IllegalArgumentException(
                "Persistent field has no definition [class=" + _types.getMainType() +
                ", field=" + field + "]");
        }
    }

    public PreparedStatement prepare (Connection conn)
        throws SQLException
    {
        if (fromOverride != null) {
            fromOverride.appendClause(_types, _builder);

        } else if (_types.getMainTableName() != null) {
            _builder.append(" from ").append(_types.getMainTableName()).
                append(" as ").append(_types.getMainTableAbbreviation());

        } else {
            throw new SQLException("Query on @Computed entity with no FromOverrideClause.");
        }

        for (Join clause : joinClauses) {
            clause.appendClause(_types, _builder);
        }
        if (where != null) {
            where.appendClause(_types, _builder);
        }
        if (groupBy != null) {
            groupBy.appendClause(_types, _builder);
        }
        if (orderBy != null) {
            orderBy.appendClause(_types, _builder);
        }
        if (limit != null) {
            limit.appendClause(_types, _builder);
        }
        if (forUpdate != null) {
            forUpdate.appendClause(_types, _builder);
        }

        PreparedStatement pstmt = conn.prepareStatement(_builder.toString());
        int argIdx = 1;
        for (Join clause : joinClauses) {
            argIdx = clause.bindClauseArguments(pstmt, argIdx);
        }
        if (where != null) {
            argIdx = where.bindClauseArguments(pstmt, argIdx);
        }
        if (groupBy != null) {
            argIdx = groupBy.bindClauseArguments(pstmt, argIdx);
        }
        if (orderBy != null) {
            argIdx = orderBy.bindClauseArguments(pstmt, argIdx);
        }
        if (limit != null) {
            argIdx = limit.bindClauseArguments(pstmt, argIdx);
        }
        if (forUpdate != null) {
            argIdx = forUpdate.bindClauseArguments(pstmt, argIdx);
        }

        return pstmt;
    }

    /** A StringBuilder to hold the constructed SQL. */
    protected StringBuilder _builder;

    /** The class that maps persistent classes to marshallers. */
    protected DepotTypes<T> _types;

    /** Persistent class fields mapped to field override clauses. */
    protected Map<String, FieldOverride> _disMap = new HashMap<String, FieldOverride>();
}
