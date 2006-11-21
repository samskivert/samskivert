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

import com.samskivert.jdbc.depot.Query;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.expression.ValueExp;

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
        public IsNull (Class pClass, String pColumn)
        {
            this(new ColumnExp(pClass, pColumn));
        }

        public IsNull (ColumnExp column)
        {
            _column = column;
        }

        // from SQLExpression
        public void appendExpression (Query query, StringBuilder builder)
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

    /** The SQL 'in (...)' operator. */
    public static class In
        implements SQLOperator
    {
        public In (Class pClass, String pColumn, Comparable... values)
        {
            this(new ColumnExp(pClass, pColumn), values);
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
        public void appendExpression (Query query, StringBuilder builder)
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

    /**
     * Does the real work for simple binary operators such as Equals.
     */
    protected abstract static class BinaryOperator implements SQLOperator
    {
        public BinaryOperator (SQLExpression lhs, SQLExpression rhs)
        {
            _lhs = lhs;
            _rhs = rhs;
        }

        public BinaryOperator (SQLExpression lhs, Comparable rhs)
        {
            this(lhs, new ValueExp(rhs));
        }

        // from SQLExpression
        public void appendExpression (Query query, StringBuilder builder)
        {
            _lhs.appendExpression(query, builder);
            builder.append(operator());
            _rhs.appendExpression(query, builder);
        }

        // from SQLExpression
        public int bindArguments (PreparedStatement pstmt, int argIdx)
            throws SQLException
        {
            argIdx = _lhs.bindArguments(pstmt, argIdx);
            argIdx = _rhs.bindArguments(pstmt, argIdx);
            return argIdx;
        }

        /**
         * Returns the string representation of the operator.
         */
        protected abstract String operator();

        protected SQLExpression _lhs;
        protected SQLExpression _rhs;
    }
}
