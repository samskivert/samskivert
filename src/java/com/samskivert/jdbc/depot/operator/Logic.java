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

/**
 * A convenient container for implementations of logical operators.  Classes that value brevity
 * over disambiguation will import Logic.* and construct queries with And() and Not(); classes that
 * feel otherwise will use Logic.And() and Logic.Not().
 */
public abstract class Logic
{
    /**
     * Represents a condition that is false iff all its subconditions are false.
     */
    public static class Or extends MultiOperator
    {
        public Or (SQLOperator... conditions)
        {
            super(conditions);
        }

        @Override
        protected String operator()
        {
            return "or";
        }
    }

    /**
     * Represents a condition that is true iff all its subconditions are true.
     */
    public static class And extends MultiOperator
    {
        public And (SQLOperator... conditions)
        {
            super(conditions);
        }

        @Override
        protected String operator()
        {
            return "and";
        }
    }


    /**
     * Represents the truth negation of another conditon.
     */
    public static class Not
        implements SQLOperator
    {
        public Not (SQLOperator condition)
        {
            super();
            _condition = condition;
        }

        // from SQLExpression
        public void appendExpression (Query query, StringBuilder builder)
        {
            builder.append(" not (");
            _condition.appendExpression(query, builder);
            builder.append(")");
        }

        // from SQLExpression
        public int bindArguments (PreparedStatement pstmt, int argIdx)
            throws SQLException
        {
            return _condition.bindArguments(pstmt, argIdx);
        }

        protected SQLOperator _condition;
    }

    /**
     * Represents an operator with any number of operands.
     */
    protected abstract static class MultiOperator
        implements SQLOperator
    {
        public MultiOperator (SQLOperator... conditions)
        {
            super();
            _conditions = conditions;
        }

        // from SQLExpression
        public void appendExpression (Query query, StringBuilder builder)
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
}
