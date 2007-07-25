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

package com.samskivert.jdbc.depot.operator;

import java.util.Collection;

import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
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
        public MultiOperator (SQLExpression ... conditions)
        {
            _conditions = conditions;
        }

        // from SQLExpression
        public void accept (ExpressionVisitor builder) throws Exception
        {
            builder.visit(this);
        }
        
        // from SQLExpression
        public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
        {
            for (int ii = 0; ii < _conditions.length; ii ++) {
                _conditions[ii].addClasses(classSet);
            }
        }

        public SQLExpression[] getConditions ()
        {
            return _conditions;
        }

        /**
         * Returns the text infix to be used to join expressions together.
         */
        public abstract String operator ();

        protected SQLExpression[] _conditions;
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
        public void accept (ExpressionVisitor builder) throws Exception
        {
            builder.visit(this);
        }

        // from SQLExpression
        public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
        {
            _lhs.addClasses(classSet);
            _rhs.addClasses(classSet);
        }
        
        /**
         * Returns the string representation of the operator.
         */
        public abstract String operator();

        public SQLExpression getLeftHandSide ()
        {
            return _lhs;
        }

        public SQLExpression getRightHandSide ()
        {
            return _rhs;
        }

        protected SQLExpression _lhs;
        protected SQLExpression _rhs;
    }
}
