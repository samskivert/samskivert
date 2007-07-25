//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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

/**
 * Builds actual SQL given a main persistent type and some {@link QueryClause} objects.
 */
public class InsertClause<T extends PersistentRecord> extends QueryClause
{
    public InsertClause (Class<? extends PersistentRecord> pClass, Object pojo, String ixField)
    {
        _pClass = pClass;
        _pojo = pojo;
        _ixField = ixField;
    }

    public Class<? extends PersistentRecord> getPersistentClass ()
    {
        return _pClass;
    }

    public Object getPojo ()
    {
        return _pojo;
    }

    public String getIndexField ()
    {
        return _ixField;
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        classSet.add(_pClass);
        // If we add SQLExpression[] values INSERT, remember to recurse into them here.
    }

    // from SQLExpression
    public void accept (ExpressionVisitor builder) throws Exception
    {
        builder.visit(this);
    }

    protected Class<? extends PersistentRecord> _pClass;

    /** The object from which to fetch values, or null. */
    protected Object _pojo;

    protected String _ixField;
}
