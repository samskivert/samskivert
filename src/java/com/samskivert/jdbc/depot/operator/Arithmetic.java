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

import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.operator.SQLOperator.BinaryOperator;

/**
 * A convenient container for implementations of arithmetic operators. Classes that value brevity
 * over disambiguation will import Arithmetic.* and construct queries with Add() and Sub(); classes
 * that feel otherwise will use Arithmetic.Add() and Arithmetic.Sub().
 */
public abstract class Arithmetic
{
    /** The SQL '+' operator. */
    public static class Add extends BinaryOperator
    {
        public Add (String pColumn, Comparable value)
        {
            super(new ColumnExp(pColumn), value);
        }

        public Add (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public Add (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        protected String operator()
        {
            return "+";
        }
    }

    /** The SQL '-' operator. */
    public static class Sub extends BinaryOperator
    {
        public Sub (String pColumn, Comparable value)
        {
            super(new ColumnExp(pColumn), value);
        }

        public Sub (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public Sub (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        protected String operator()
        {
            return "-";
        }
    }

    /** The SQL '*' operator. */
    public static class Mul extends BinaryOperator
    {
        public Mul (String pColumn, Comparable value)
        {
            super(new ColumnExp(pColumn), value);
        }

        public Mul (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public Mul (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        protected String operator()
        {
            return "*";
        }
    }

    /** The SQL '/' operator. */
    public static class Div extends BinaryOperator
    {
        public Div (String pColumn, Comparable value)
        {
            super(new ColumnExp(pColumn), value);
        }

        public Div (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public Div (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        protected String operator()
        {
            return "/";
        }
    }

    /** The SQL '&' operator. */
    public static class BitAnd extends BinaryOperator
    {
        public BitAnd (String pColumn, Comparable value)
        {
            super(new ColumnExp(pColumn), value);
        }

        public BitAnd (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public BitAnd (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        protected String operator()
        {
            return "&";
        }
    }

    /** The SQL '|' operator. */
    public static class BitOr extends BinaryOperator
    {
        public BitOr (String pColumn, Comparable value)
        {
            super(new ColumnExp(pColumn), value);
        }

        public BitOr (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public BitOr (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        protected String operator()
        {
            return "|";
        }
    }
}
