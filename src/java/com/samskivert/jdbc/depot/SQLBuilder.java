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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.FullTextIndex;
import com.samskivert.jdbc.depot.annotation.GeneratedValue;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.operator.Conditionals;

/**
 * At the heart of Depot's SQL generation, this object constructs two {@link ExpressionVisitor}
 * objects and executes them, one after another; the first one constructs SQL as it recurses, the
 * other binds arguments in the {@link PreparedStatement}. This class must be subclassed by the
 * database dialects we wish to support.
 */
public abstract class SQLBuilder
{
    public SQLBuilder (DepotTypes types)
    {
        _types = types;
    }

    /**
     * Construct an entirely new SQL query relative to our configured {@link DepotTypes} data.
     * This method may be called multiple times, each time beginning a new query, and should be
     * followed up by a call to {@link #prepare(Connection)} which creates, configures and
     * returns the actual {@link PreparedStatement} to execute.
     */
    public void newQuery (QueryClause clause)
    {
        _clause = clause;
        _buildVisitor = getBuildVisitor();

        try {
            _clause.accept(_buildVisitor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build SQL", e);
        }
    }

    /**
     * After {@link #newQuery(QueryClause)} has been executed, this method is run to recurse
     * through the {@link QueryClause} structure, setting the {@link PreparedStatement} arguments
     * that were defined in the generated SQL.
     *
     * This method throws {@link SQLException} and is thus meant to be called from within
     * {@link Query#invoke(Connection)} and {@link Modifier#invoke(Connection)}.
     */
    public PreparedStatement prepare (Connection conn)
        throws SQLException
    {
        if (_buildVisitor == null) {
            throw new IllegalArgumentException("Cannot prepare query until it's been built.");
        }
        PreparedStatement stmt = conn.prepareStatement(_buildVisitor.getQuery());
        _bindVisitor = getBindVisitor(stmt);

        try {
            _clause.accept(_bindVisitor);
        } catch (SQLException se) {
            throw se;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find SQL parameters", e);
        }
        return stmt;
    }

    /**
     * Generates the SQL needed to construct a database column for field represented by the given
     * {@link FieldMarshaller}.
     *
     * TODO: This method should be split into several parts that are more easily overridden on a
     * case-by-case basis in the dialectal subclasses.
     */
    public <T> String buildColumnDefinition (FieldMarshaller<T> fm)
    {
        // if this field is @Computed, it has no SQL definition
        if (fm.getComputed() != null) {
            return null;
        }

        Field field = fm.getField();

        // read our column metadata from the annotation (if it exists); annoyingly we can't create
        // a Column instance to read the defaults so we have to duplicate them here
        int length = 255;
        boolean nullable = false;
        boolean unique = false;
        String type = "";
        String defval = "";
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            nullable = column.nullable();
            unique = column.unique();
            length = column.length();
            type = column.type();
            defval = column.defaultValue();
        }

        // create our SQL column definition
        StringBuilder builder = new StringBuilder();
        boolean typeDone = false;

        // handle primary keyness
        GeneratedValue genValue = fm.getGeneratedValue();
        if (genValue != null) {
            switch (genValue.strategy()) {
            case AUTO:
            case IDENTITY:
                builder.append(" SERIAL UNIQUE");
                typeDone = true;
                break;
            case SEQUENCE: // TODO
                throw new IllegalArgumentException(
                    "SEQUENCE key generation strategy not yet supported.");
            case TABLE:
                // nothing to do here, it'll be handled later
                break;
            }
        }

        if (!typeDone) {
            if (type.length() == 0) {
                type = getColumnType(fm, length);
            }
            builder.append(" ").append(type);

            // TODO: handle precision and scale

            // handle nullability and uniqueness
            if (!nullable) {
                builder.append(" NOT NULL");
            }
            if (unique) {
                builder.append(" UNIQUE");
            }

            // append the default value if one was specified
            if (defval.length() > 0) {
                builder.append(" DEFAULT ").append(defval);
            }
        }

        return builder.toString();
    }

    /**
     * Add full-text search capabilities, as defined by the provided {@link FullTextIndex}, on
     * the table associated with the given {@link DepotMarshaller}. This is a highly database
     * specific operation and must thus be implemented by each dialect subclass.
     *
     * {@see Conditionals.FullTextIndex}
     */
    public abstract <T extends PersistentRecord> boolean addFullTextSearch (
        Connection conn, DepotMarshaller<T> marshaller, FullTextIndex fts)
        throws SQLException;

    /**
     * Return true if the supplied column is an internal consideration of this {@link SQLBuilder},
     * e.g. PostgreSQL's full text search data is stored in a table column that should otherwise
     * not be visible to Depot; this method helps mask it.
     */
    public abstract boolean isPrivateColumn (String column);

    /**
     * Overridden by subclasses to create a dialect-specific {@link BuildVisitor}.
     */
    protected abstract BuildVisitor getBuildVisitor ();

    /**
     * Overridden by subclasses to create a dialect-specific {@link BindVisitor}.
     */
    protected abstract BindVisitor getBindVisitor (PreparedStatement stmt);

    /**
     * Overridden by subclasses to figure the dialect-specific SQL type of the given field.
     * @param length
     */
    protected abstract <T> String getColumnType (FieldMarshaller fm, int length);

    /** The class that maps persistent classes to marshallers. */
    protected DepotTypes _types;

    protected QueryClause _clause;
    protected BuildVisitor _buildVisitor;
    protected BindVisitor _bindVisitor;
}
