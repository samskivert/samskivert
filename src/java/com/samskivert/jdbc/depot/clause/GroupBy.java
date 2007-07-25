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
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 *  Represents a GROUP BY clause.
 */
public class GroupBy extends QueryClause
{
    public GroupBy (SQLExpression... values)
    {
        _values = values;
    }

    public SQLExpression[] getValues ()
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
        // I can't imagine a GROUP BY clause bringing in new tables... ?
    }

    /** The expressions that are generated for the clause. */
    protected SQLExpression[] _values;

}
