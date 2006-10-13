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
import com.samskivert.jdbc.depot.clause.FieldOverrideClause;
import com.samskivert.jdbc.depot.clause.GroupByClause;
import com.samskivert.jdbc.depot.clause.JoinClause;
import com.samskivert.jdbc.depot.clause.LimitClause;
import com.samskivert.jdbc.depot.clause.OrderByClause;
import com.samskivert.jdbc.depot.clause.QueryClause;

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
        if (cl.equals(_mainType)) {
            return "T";
        }
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

        DepotMarshaller mainMarshaller = _classMap.get(_mainType);
        String[] fields = mainMarshaller._allFields;
        StringBuilder query = new StringBuilder("select ");
        for (int ii = 0; ii < fields.length; ii ++) {
            if (ii > 0) {
                query.append(", ");
            }
            FieldOverrideClause clause = _disMap.get(fields[ii]);
            if (clause != null) {
                clause.appendClause(this, query);
            } else {
                query.append("T.").append(fields[ii]);
            }
        }
        query.append("   from " + mainMarshaller.getTableName() + " as T ");

        for (JoinClause clause : _joinClauses) {
            query.append(" inner join " );
            clause.appendClause(this, query);
        }
        if (_key != null) {
            query.append(" where ");
            _key.appendClause(this, query);
        }
        if (_groupBy != null) {
            query.append(" group by ");
            _groupBy.appendClause(this, query);
        }
        if (_orderBy != null) {
            query.append(" order by ");
            _orderBy.appendClause(this, query);
        }
        if (_limit != null) {
            query.append(" limit ");
            _limit.appendClause(this, query);
        }

        PreparedStatement pstmt = conn.prepareStatement(query.toString());
        int argIdx = 1;
        if (_key != null) {
            argIdx = _key.bindArguments(pstmt, argIdx);
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

        return pstmt;
    }

    /**
     * Creates a new Query object to generate one or more instances of the specified persistent
     * class, as dictated by the key and query clauses.  A persistence context is supplied for
     * instantiation of marshallers, which may trigger table creations and schema migrations.
     */
    protected Query (PersistenceContext ctx, Class type, Key key, QueryClause... clauses)
        throws PersistenceException
    {
        _key = key;
        _mainType = type;
        Set<Class> classSet = new HashSet<Class>();
        if (type != null) {
            classSet.add(type);
        }
        if (_key != null) {
            classSet.addAll(_key.getClassSet());
        }

        for (QueryClause clause : clauses) {
            if (clause instanceof JoinClause) {
                _joinClauses.add((JoinClause) clause);
                classSet.addAll(((JoinClause) clause).getClassSet());

            } else if (clause instanceof FieldOverrideClause) {
                _disMap.put(((FieldOverrideClause) clause).getField(),
                            ((FieldOverrideClause) clause));

            } else if (clause instanceof OrderByClause) {
                if (_orderBy != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple OrderBy clauses.");
                }
                _orderBy = (OrderByClause) clause;

            } else if (clause instanceof GroupByClause) {
                if (_groupBy != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple GroupBy clauses.");
                }
                _groupBy = (GroupByClause) clause;

            } else if (clause instanceof LimitClause) {
                if (_limit != null) {
                    throw new IllegalArgumentException(
                        "Query can't contain multiple Limit clauses.");
                }
                _limit = (LimitClause) clause;
            }
        }
        _classMap = new HashMap<Class, DepotMarshaller>();

        for (Class<?> c : classSet) {
            _classMap.put(c, ctx.getMarshaller(c));
        }
        _classList = new ArrayList<Class>(classSet);
    }

    /** The key to this query, or null. Translated into a WHERE clause. */
    protected Key _key;

    /** The persistent class to instantiate for the results. */
    protected Class _mainType;

    /** A list of referenced classes, used to generate table abbreviations. */
    protected List<Class> _classList;

    /** Classes mapped to marshallers, used for table names and field lists. */
    protected Map<Class, DepotMarshaller> _classMap;

    /** Persistent class fields mapped to field override clauses. */
    protected Map<String, FieldOverrideClause> _disMap = new HashMap<String, FieldOverrideClause>();

    /** A list of join clauses, each potentially referencing a new class. */
    protected List<JoinClause> _joinClauses = new ArrayList<JoinClause>();

    /** The order by clause, if any. */
    protected OrderByClause _orderBy;

    /** The group by clause, if any. */
    protected GroupByClause _groupBy;

    /** The limit clause, if any. */
    protected LimitClause _limit;
}
