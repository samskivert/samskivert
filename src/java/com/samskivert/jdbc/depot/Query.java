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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.samskivert.io.PersistenceException;
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
 * Encapsulates a non-modifying query of persistent objects.
 */
public abstract class Query<T>
{
    /**
     * Performs the actual JDBC operations associated with this query.
     */
    public abstract T invoke (Connection conn) throws SQLException;

    /**
     * Maps a class referenced by this query to its associated table.
     *
     * This method is called by individual clauses.
     */
    public String getTableName (Class cl)
    {
        return _classMap.get(cl).getTableName();
    }

    /**
     * Maps a class referenced by this query to the abbreviation used for its associated table. The
     * abbreviation is transientand only has meaning within the scope of this particular query.
     *
     * This method is called by individual clauses.
     */
    public String getTableAbbreviation (Class cl)
    {
        int ix = _classList.indexOf(cl);
        if (ix < 0) {
            throw new IllegalArgumentException("Unknown persistence class: " + cl);
        }
        return "T" + (ix+1);
    }

    /**
     * Translates the data in this Query object into a SQL query suited to instantiate a persistent
     * object given the supplied key and clauses.  These clauses are assumed to contain no
     * conflicts.
     *
     * If no key is supplied all instances will be loaded.
     */
    public <T> PreparedStatement createQuery (Connection conn)
        throws SQLException
    {
        if (_mainType == null) { // internal error
            throw new RuntimeException("createQuery() called with _mainClass == null");
        }

        DepotMarshaller<?> mainMarshaller = _classMap.get(_mainType);
        String[] fields = mainMarshaller._allFields;
        StringBuilder query = new StringBuilder("select ");
        boolean skip = true;
        for (int ii = 0; ii < fields.length; ii ++) {
            if (!skip) {
                query.append(", ");
            }
            skip = false;

            FieldOverride clause = _disMap.get(fields[ii]);
            if (clause != null) {
                clause.appendClause(this, query);
                continue;
            }

            Computed computed = mainMarshaller._fields.get(fields[ii]).getComputed();
            if (computed == null) {
                // make sure the object corresponds to a table, otherwise the whole thing is
                // computed
                if (mainMarshaller.getTableName() != null) {
                    // if it's neither overridden nor computed, it's a standard field
                    query.append(getTableAbbreviation(_mainType)).append(".").append(fields[ii]);
                    continue;
                }
                throw new SQLException(
                    "@Computed entity field without definition [field=" + fields[ii] + "]");
            }

            // check if the computed field has a literal SQL definition
            if (computed.fieldDefinition().length() > 0) {
                query.append(computed.fieldDefinition() + " as " + fields[ii]);

            } else if (!computed.required()) {
                // or if we can simply ignore the field
                skip = true;

            } else {
                throw new SQLException(
                    "@Computed(required) field without definition [field=" + fields[ii] + "]");
            }
        }

        if (_fromOverride != null) {
            _fromOverride.appendClause(this, query);
        } else if (mainMarshaller.getTableName() != null) {
            query.append(" from ").append(mainMarshaller.getTableName());
            query.append(" as ").append(getTableAbbreviation(_mainType));
        } else {
            throw new SQLException("Query on @Computed entity with no FromOverrideClause.");
        }

        for (Join clause : _joinClauses) {
            clause.appendClause(this, query);
        }
        if (_where != null) {
            _where.appendClause(this, query);
        }
        if (_groupBy != null) {
            _groupBy.appendClause(this, query);
        }
        if (_orderBy != null) {
            _orderBy.appendClause(this, query);
        }
        if (_limit != null) {
            _limit.appendClause(this, query);
        }
        if (_forUpdate != null) {
            _forUpdate.appendClause(this, query);
        }

        PreparedStatement pstmt = conn.prepareStatement(query.toString());
        int argIdx = 1;
        if (_where != null) {
            argIdx = _where.bindArguments(pstmt, argIdx);
        }
        if (_groupBy != null) {
            argIdx = _groupBy.bindArguments(pstmt, argIdx);
        }
        if (_orderBy != null) {
            argIdx = _orderBy.bindArguments(pstmt, argIdx);
        }
        if (_limit != null) {
            argIdx = _limit.bindArguments(pstmt, argIdx);
        }
        if (_forUpdate != null) {
            argIdx = _forUpdate.bindArguments(pstmt, argIdx);
        }

        return pstmt;
    }

    /**
     * Creates a new Query object to generate one or more instances of the specified persistent
     * class, as dictated by the key and query clauses.  A persistence context is supplied for
     * instantiation of marshallers, which may trigger table creations and schema migrations.
     */
    protected Query (PersistenceContext ctx, Class type, QueryClause... clauses)
        throws PersistenceException
    {
        _mainType = type;
        Set<Class> classSet = new HashSet<Class>();
        if (type != null) {
            classSet.add(type);
        }

        for (QueryClause clause : clauses) {
            if (clause instanceof Where) {
                if (_where != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple Where clauses.");
                }
                _where = (Where) clause;

            } else if (clause instanceof FromOverride) {
                if (_fromOverride != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple FromOverride clauses.");
                }
                _fromOverride = (FromOverride) clause;
                classSet.addAll(_fromOverride.getClassSet());

            } else if (clause instanceof Join) {
                _joinClauses.add((Join) clause);
                classSet.addAll(((Join) clause).getClassSet());

            } else if (clause instanceof FieldOverride) {
                _disMap.put(((FieldOverride) clause).getField(),
                            ((FieldOverride) clause));

            } else if (clause instanceof OrderBy) {
                if (_orderBy != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple OrderBy clauses.");
                }
                _orderBy = (OrderBy) clause;

            } else if (clause instanceof GroupBy) {
                if (_groupBy != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple GroupBy clauses.");
                }
                _groupBy = (GroupBy) clause;

            } else if (clause instanceof Limit) {
                if (_limit != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple Limit clauses.");
                }
                _limit = (Limit) clause;

            } else if (clause instanceof ForUpdate) {
                if (_forUpdate != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple For Update clauses.");
                }
                _forUpdate = (ForUpdate) clause;
            }
        }
        _classMap = new HashMap<Class, DepotMarshaller>();

        for (Class<?> c : classSet) {
            _classMap.put(c, ctx.getMarshaller(c));
        }
        _classList = new ArrayList<Class>(classSet);
    }

    /** The persistent class to instantiate for the results. */
    protected Class _mainType;

    /** A list of referenced classes, used to generate table abbreviations. */
    protected List<Class> _classList;

    /** Classes mapped to marshallers, used for table names and field lists. */
    protected Map<Class, DepotMarshaller> _classMap;

    /** Persistent class fields mapped to field override clauses. */
    protected Map<String, FieldOverride> _disMap = new HashMap<String, FieldOverride>();

    /** The from override clause, if any. */
    protected FromOverride _fromOverride;

    /** The where clause. */
    protected Where _where;

    /** A list of join clauses, each potentially referencing a new class. */
    protected List<Join> _joinClauses = new ArrayList<Join>();

    /** The order by clause, if any. */
    protected OrderBy _orderBy;

    /** The group by clause, if any. */
    protected GroupBy _groupBy;

    /** The limit clause, if any. */
    protected Limit _limit;

    /** The For Update clause, if any. */
    protected ForUpdate _forUpdate;
}
