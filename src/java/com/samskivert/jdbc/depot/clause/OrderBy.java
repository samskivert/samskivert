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

package com.samskivert.jdbc.depot.clause;

import java.util.Collection;

import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
import com.samskivert.jdbc.depot.expression.LiteralExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 *  Represents an ORDER BY clause.
 */
public class OrderBy extends QueryClause
{
    /** Indicates the order of the clause. */
    public enum Order { ASC, DESC };

    /**
     * Creates and returns a random order by clause.
     */
    public static OrderBy random ()
    {
        return ascending(new LiteralExp("rand()"));
    }

    /**
     * Creates and returns an ascending order by clause on the supplied expression.
     */
    public static OrderBy ascending (SQLExpression value)
    {
        return new OrderBy(new SQLExpression[] { value } , new Order[] { Order.ASC });
    }

    /**
     * Creates and returns a descending order by clause on the supplied expression.
     */
    public static OrderBy descending (SQLExpression value)
    {
        return new OrderBy(new SQLExpression[] { value }, new Order[] { Order.DESC });
    }

    public OrderBy (SQLExpression[] values, Order[] orders)
    {
        _values = values;
        _orders = orders;
    }

    public SQLExpression[] getValues ()
    {
        return _values;
    }

    public Order[] getOrders ()
    {
        return _orders;
    }

    // from SQLExpression
    public void accept (ExpressionVisitor builder) throws Exception
    {
        builder.visit(this);
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        for (SQLExpression expression : _values) {
            expression.addClasses(classSet);
        }
    }

    /** The expressions that are generated for the clause. */
    protected SQLExpression[] _values;

    /** Whether the ordering is to be ascending or descending. */
    protected Order[] _orders;

}
