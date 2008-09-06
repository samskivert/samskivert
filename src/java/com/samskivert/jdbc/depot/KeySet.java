//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2008 Michael Bayne
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

package com.samskivert.jdbc.depot;

import java.util.Collection;

import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
import com.samskivert.jdbc.depot.expression.LiteralExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.operator.Conditionals;
import com.samskivert.jdbc.depot.operator.Logic;

/**
 * Contains a set of primary keys that match a set of persistent records. This is used internally
 * in Depot when decomposing queries into two parts: first a query for the primary keys that
 * identify the records that match a free-form query and then another query that operates on the
 * previously identified keys. The keys obtained in the first query are used to create a KeySet and
 * modifications and deletons using this set will automatically flush the appropriate records from
 * the cache.
 */
public class KeySet<T extends PersistentRecord> extends WhereClause
    implements SQLExpression, ValidatingCacheInvalidator
{
    /**
     * Creates a set from the supplied primary keys.
     */
    public KeySet (Class<T> pClass, Collection<Key<T>> keys)
    {
        _pClass = pClass;
        _keys = keys;

        String[] keyFields = KeyUtil.getKeyFields(pClass);
        if (keys.size() == 0) {
            _condition = new LiteralExp("false");

        } else if (keyFields.length == 1) {
            if (keys.size() > Conditionals.In.MAX_KEYS) {
                throw new IllegalArgumentException("Cannot create where clause for more than " +
                                                   Conditionals.In.MAX_KEYS + " at a time.");
            }

            // Single-column keys result in the compact IN(keyVal1, keyVal2, ...)
            Comparable<?>[] keyFieldValues = new Comparable<?>[keys.size()];
            int ii = 0;
            for (Key<T> key : keys) {
                keyFieldValues[ii++] = key.getValues().get(0);
            }
            _condition = new Conditionals.In(pClass, keyFields[0], keyFieldValues);

        } else {
            // TODO: is there a maximum size of an or query? 32768?

            // Multi-column keys result in OR'd AND's, of unknown efficiency (TODO check).
            SQLExpression[] keyArray = new SQLExpression[keys.size()];
            int ii = 0;
            for (Key<T> key : keys) {
                keyArray[ii++] = key;
            }
            _condition = new Logic.Or(keyArray);
        }
    }

    // from WhereClause
    public SQLExpression getWhereExpression ()
    {
        return _condition;
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        classSet.add(_pClass);
    }

    // from SQLExpression
    public void accept (ExpressionVisitor builder)
    {
        builder.visit(this);
    }

    // from ValidatingCacheInvalidator
    public void validateFlushType (Class<?> pClass)
    {
        if (!pClass.equals(_pClass)) {
            throw new IllegalArgumentException(
                "Class mismatch between persistent record and cache invalidator " +
                "[record=" + pClass.getSimpleName() + ", invtype=" + _pClass.getSimpleName() + "].");
        }
    }

    // from CacheInvalidator
    public void invalidate (PersistenceContext ctx)
    {
        for (Key<T> key : _keys) {
            ctx.cacheInvalidate(key);
        }
    }

    @Override // from WhereClause
    public void validateQueryType (Class<?> pClass)
    {
        super.validateQueryType(pClass);
        validateTypesMatch(pClass, _pClass);
    }

    @Override
    public boolean equals (Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return _condition.equals(((KeySet<?>) obj)._condition);
    }

    @Override
    public int hashCode ()
    {
        return _condition.hashCode();
    }

    @Override
    public String toString ()
    {
        return _keys.toString();
    }

    protected Class<T> _pClass;
    protected Collection<Key<T>> _keys;
    protected SQLExpression _condition;
}
