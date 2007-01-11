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

import com.samskivert.jdbc.depot.ConstructedQuery;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.expression.ValueExp;

/**
 * A common interface for operator hierarchies in SQL. The main purpose of breaking this out from
 * SQLExpression is to capture the recursive nature of e.g. the logical operators, which work on
 * other SQLOperators but not general SQLExpressions.
 */
public interface SQLOperator extends SQLExpression
{
    /**
     * Represents an operator with any number of operands.
     */
    public abstract static class MultiOperator
        implements SQLOperator
    {
        public MultiOperator (SQLOperator... conditions)
        {
            super();
            _conditions = conditions;
        }

        // from SQLExpression
        public void appendExpression (ConstructedQuery query, StringBuilder builder)
        {
            for (int ii = 0; ii < _conditions.length; ii++) {
                if (ii > 0) {
                    builder.append(" ").append(operator()).append(" ");
                }
                builder.append("(");
                _conditions[ii].appendExpression(query, builder);
                builder.append(")");
            }
        }

        // from SQLExpression
        public int bindArguments (PreparedStatement pstmt, int argIdx)
            throws SQLException
        {
            for (int ii = 0; ii < _conditions.length; ii++) {
                argIdx = _conditions[ii].bindArguments(pstmt, argIdx);
            }
            return argIdx;
        }

        /**
         * Returns the text infix to be used to join expressions together.
         */
        protected abstract String operator ();

        protected SQLOperator[] _conditions;
    }

    /**
     * Does the real work for simple binary operators such as Equals.
     */
    public abstract static class BinaryOperator implements SQLOperator
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
        public void appendExpression (ConstructedQuery query, StringBuilder builder)
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
