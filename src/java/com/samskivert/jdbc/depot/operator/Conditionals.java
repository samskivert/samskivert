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
import com.samskivert.jdbc.depot.clause.SelectClause;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
import com.samskivert.jdbc.depot.expression.SQLExpression;

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

        public IsNull (Class<? extends PersistentRecord> pClass, String pColumn)
        {
            this(new ColumnExp(pClass, pColumn));
        }

        public IsNull (ColumnExp column)
        {
            _column = column;
        }

        public ColumnExp getColumn()
        {
            return _column;
        }

        // from SQLExpression
        public void accept (ExpressionVisitor builder) throws Exception
        {
            builder.visit(this);
        }

        // from SQLExpression
        public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
        {
        }

        protected ColumnExp _column;
    }

    /** The SQL '=' operator. */
    public static class Equals extends SQLOperator.BinaryOperator
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
        public String operator()
        {
            return "=";
        }
    }

    /** The SQL '<' operator. */
    public static class LessThan extends SQLOperator.BinaryOperator
    {
        public LessThan (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public LessThan (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        public String operator()
        {
            return "<";
        }
    }

    /** The SQL '<=' operator. */
    public static class LessThanEquals extends SQLOperator.BinaryOperator
    {
        public LessThanEquals (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public LessThanEquals (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        public String operator()
        {
            return "<=";
        }
    }

    /** The SQL '>' operator. */
    public static class GreaterThan extends SQLOperator.BinaryOperator
    {
        public GreaterThan (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public GreaterThan (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        public String operator()
        {
            return ">";
        }
    }

    /** The SQL '>=' operator. */
    public static class GreaterThanEquals extends SQLOperator.BinaryOperator
    {
        public GreaterThanEquals (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public GreaterThanEquals (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        public String operator()
        {
            return ">=";
        }
    }

    /** The SQL 'in (...)' operator. */
    public static class In
        implements SQLOperator
    {
        public In (Class<? extends PersistentRecord> pClass, String pColumn, Comparable... values)
        {
            this(new ColumnExp(pClass, pColumn), values);
        }

        public In (Class<? extends PersistentRecord> pClass, String pColumn,
                   Collection<? extends Comparable> values)
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

        public In (ColumnExp pColumn, Collection<? extends Comparable> values)
        {
            this(pColumn, values.toArray(new Comparable[values.size()]));
        }

        public ColumnExp getColumn ()
        {
            return _column;
        }

        public Comparable[] getValues ()
        {
            return _values;
        }

        // from SQLExpression
        public void accept (ExpressionVisitor builder) throws Exception
        {
            builder.visit(this);
        }

        // from SQLExpression
        public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
        {
            _column.addClasses(classSet);
        }

        protected ColumnExp _column;
        protected Comparable[] _values;
    }

    /** The SQL ' like ' operator. */
    public static class Like extends SQLOperator.BinaryOperator
    {
        public Like (SQLExpression column, Comparable value)
        {
            super(column, value);
        }

        public Like (SQLExpression column, SQLExpression value)
        {
            super(column, value);
        }

        @Override
        public String operator()
        {
            return " like ";
        }
    }

    /** The SQL ' exists' operator. */
    public static class Exists<T extends PersistentRecord> implements SQLOperator
    {
        public Exists (SelectClause<T> clause)
        {
            _clause = clause;
        }

        public void accept (ExpressionVisitor builder) throws Exception
        {
            builder.visit(this);
        }

        public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
        {
            _clause.addClasses(classSet);
        }

        public SelectClause<T> getSubClause ()
        {
            return _clause;
        }

        protected SelectClause<T> _clause;
    }

    /**
     * An attempt at a dialect-agnostic full-text search condition, such as MySQL's MATCH() and
     * PostgreSQL's @@ TO_TSQUERY(...) abilities.
     */
    public static class FullTextMatch
        implements SQLOperator
    {
        public FullTextMatch (Class<? extends PersistentRecord> pClass, String name, String query)
        {
            _pClass = pClass;
            _name = name;
            _query = query;
        }

        public Class<? extends PersistentRecord> getPersistentRecord ()
        {
            return _pClass;
        }

        public String getQuery ()
        {
            return _query;
        }

        public String getName ()
        {
            return _name;
        }

        // from SQLExpression
        public void accept (ExpressionVisitor builder) throws Exception
        {
            builder.visit(this);
        }

        // from SQLExpression
        public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
        {
        }

        protected Class<? extends PersistentRecord> _pClass;
        protected String _name;
        protected String _query;
    }
}
