//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne, Pär Winzell
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

package com.samskivert.jdbc.depot.operator;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import com.samskivert.jdbc.depot.ConstructedQuery;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.operator.SQLOperator.BinaryOperator;

/**
 * A convenient container for implementations of conditional operators.  Classes that value brevity
 * over disambiguation will import Conditionals.* and construct queries with Equals() and In();
 * classes that feel otherwise will use Conditionals.Equals() and Conditionals.In().
 */
public abstract class Conditionals
{
    /** The SQL 'is null' operator. */
    public static class IsNull
        implements SQLOperator
    {
        public IsNull (String pColumn)
        {
            this(new ColumnExp(null, pColumn));
        }

        public IsNull (Class pClass, String pColumn)
        {
            this(new ColumnExp(pClass, pColumn));
        }

        public IsNull (ColumnExp column)
        {
            _column = column;
        }

        // from SQLExpression
        public void appendExpression (ConstructedQuery query, StringBuilder builder)
        {
            _column.appendExpression(query, builder);
            builder.append(" is null");
        }

        // from SQLExpression
        public int bindArguments (PreparedStatement pstmt, int argIdx)
            throws SQLException
        {
            return argIdx;
        }

        protected ColumnExp _column;
    }

    /** The SQL '=' operator. */
    public static class Equals extends BinaryOperator
    {
        public Equals (String pColumn, Comparable value)
        {
            super(new ColumnExp(pColumn), value);
        }

        public Equals (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public Equals (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        protected String operator()
        {
            return "=";
        }
    }

    /** The SQL '<' operator. */
    public static class LessThan extends BinaryOperator
    {
        public LessThan (String pColumn, Comparable value)
        {
            super(new ColumnExp(pColumn), value);
        }

        public LessThan (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public LessThan (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        protected String operator()
        {
            return "<";
        }
    }

    /** The SQL '>' operator. */
    public static class GreaterThan extends BinaryOperator
    {
        public GreaterThan (String pColumn, Comparable value)
        {
            super(new ColumnExp(pColumn), value);
        }

        public GreaterThan (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public GreaterThan (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        protected String operator()
        {
            return ">";
        }
    }

    /** The SQL 'in (...)' operator. */
    public static class In
        implements SQLOperator
    {
        public In (String pColumn, Comparable... values)
        {
            this(new ColumnExp(null, pColumn), values);
        }

        public In (String pColumn, Collection<? extends Comparable> values)
        {
            this(new ColumnExp(null, pColumn), values.toArray(new Comparable[values.size()]));
        }

        public In (Class pClass, String pColumn, Comparable... values)
        {
            this(new ColumnExp(pClass, pColumn), values);
        }

        public In (Class pClass, String pColumn, Collection<? extends Comparable> values)
        {
            this(new ColumnExp(pClass, pColumn), values.toArray(new Comparable[values.size()]));
        }

        public In (ColumnExp column, Comparable... values)
        {
            if (values.length == 0) {
                throw new IllegalArgumentException("In() condition needs at least one value.");
            }
            _column = column;
            _values = values;
        }

        // from SQLExpression
        public void appendExpression (ConstructedQuery query, StringBuilder builder)
        {
            _column.appendExpression(query, builder);
            builder.append(" in (");
            for (int ii = 0; ii < _values.length; ii ++) {
                if (ii > 0) {
                    builder.append(", ");
                }
                builder.append("?");
            }
            builder.append(")");
        }

        // from SQLExpression
        public int bindArguments (PreparedStatement pstmt, int argIdx)
            throws SQLException
        {
            for (int ii = 0; ii < _values.length; ii++) {
                pstmt.setObject(argIdx ++, _values[ii]);
            }
            return argIdx;
        }

        protected ColumnExp _column;
        protected Comparable[] _values;
    }

    /** The MySQL 'match (...) against (...)' operator. */
    public static class Match
        implements SQLOperator
    {
        public enum Mode { DEFAULT, BOOLEAN, NATURAL_LANGUAGE };

        public Match (String query, Mode mode, boolean queryExpansion, String... pColumns)
        {
            _query = query;
            _mode = mode;
            _queryExpansion = queryExpansion;
            _columns = new ColumnExp[pColumns.length];
            for (int ii = 0; ii < pColumns.length; ii++) {
                _columns[ii] = new ColumnExp(null, pColumns[ii]);
            }
        }

        public Match (String query, Mode mode, boolean queryExpansion, ColumnExp... columns)
        {
            _query = query;
            _queryExpansion = queryExpansion;
            _mode = mode;
            _columns = columns;
        }

        // from SQLExpression
        public void appendExpression (ConstructedQuery query, StringBuilder builder)
        {
            builder.append("match(");
            int idx = 0;
            for (ColumnExp column : _columns) {
                if (idx++ > 0) {
                    builder.append(", ");
                }
                column.appendExpression(query, builder);
            }
            builder.append(") against (?");
            switch (_mode) {
            case BOOLEAN:
                builder.append(" in boolean mode");
                break;
            case NATURAL_LANGUAGE:
                builder.append(" in natural language mode");
                break;
            }
            if (_queryExpansion) {
                builder.append(" with query expansion");
            }
            builder.append(")");
        }

        // from SQLExpression
        public int bindArguments (PreparedStatement pstmt, int argIdx)
            throws SQLException
        {
            pstmt.setString(argIdx++, _query);
            return argIdx;
        }

        protected String _query;
        protected Mode _mode;
        protected boolean _queryExpansion;
        protected ColumnExp[] _columns;
    }
}
