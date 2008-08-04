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
import com.samskivert.jdbc.depot.WhereClause;
import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 * Builds actual SQL given a main persistent type and some {@link QueryClause} objects.
 */
public class UpdateClause<T extends PersistentRecord> extends QueryClause
{
    public UpdateClause (Class<? extends PersistentRecord> pClass, WhereClause where,
                         String[] fields, T pojo)
    {
        _pClass = pClass;
        _where = where;
        _fields = fields;
        _values = null;
        _pojo = pojo;
    }

    public UpdateClause (Class<? extends PersistentRecord> pClass, WhereClause where,
                         String[] fields, SQLExpression[] values)
    {
        _pClass = pClass;
        _fields = fields;
        _where = where;
        _values = values;
        _pojo = null;
    }

    public WhereClause getWhereClause ()
    {
        return _where;
    }

    public String[] getFields ()
    {
        return _fields;
    }

    public SQLExpression[] getValues ()
    {
        return _values;
    }

    public Object getPojo ()
    {
        return _pojo;
    }

    public Class<? extends PersistentRecord> getPersistentClass ()
    {
        return _pClass;
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        classSet.add(_pClass);
        if (_where != null) {
            _where.addClasses(classSet);
        }
        if (_values != null) {
            for (int ii = 0; ii < _values.length; ii ++) {
                _values[ii].addClasses(classSet);
            }
        }
    }

    // from SQLExpression
    public void accept (ExpressionVisitor builder) throws Exception
    {
        builder.visit(this);
    }

    /** The class we're updating. */
    protected Class<? extends PersistentRecord> _pClass;

    /** The where clause. */
    protected WhereClause _where;

    /** The persistent fields to update. */
    protected String[] _fields;

    /** The field values, or null. */
    protected SQLExpression[] _values;

    /** The object from which to fetch values, or null. */
    protected Object _pojo;
}
