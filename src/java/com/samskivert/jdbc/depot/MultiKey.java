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

package com.samskivert.jdbc.depot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.samskivert.jdbc.depot.clause.Where;
import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 * A special form of {@link Where} clause that specifies an explicit range of database rows. It
 * does not implement {@link CacheKey} but it does implement {@link CacheInvalidator} which means
 * it can be sent into e.g. {@link DepotRepository#deleteAll) and have it clean up after itself.
 */
public class MultiKey<T extends PersistentRecord> extends WhereClause
    implements ValidatingCacheInvalidator
{
    /**
     * Constructs a new single-column {@code MultiKey} with the given value range.
     */
    public MultiKey (Class<T> pClass, String ix, Comparable<?>... val)
    {
        this(pClass, new String[0], new Comparable[0], ix, val);
    }

    /**
     * Constructs a new two-column {@code MultiKey} with the given value range.
     */
    public MultiKey (Class<T> pClass, String ix1, Comparable<?> val1, String ix2,
        Comparable<?>... val2)
    {
        this(pClass, new String[] { ix1 }, new Comparable[] { val1 }, ix2, val2);
    }

    /**
     * Constructs a new three-column {@code MultiKey} with the given value range.
     */
    public MultiKey (Class<T> pClass, String ix1, Comparable<?> val1,
                     String ix2, Comparable<?> val2, String ix3, Comparable<?>... val3)
    {
        this(pClass, new String[] { ix1, ix2 }, new Comparable[] { val1, val2 }, ix3, val3);
    }

    /**
     * Constructs a new multi-column {@code MultiKey} with the given value range.
     * @TODO: See {@link Key#Key(Class, String[], Comparable[]) for somewhat relevant comments.
     */
    public MultiKey (Class<T> pClass, String[] sFields, Comparable<?>[] sValues,
                     String mField, Comparable<?>[] mValues)
    {
        if (sFields.length != sValues.length) {
            throw new IllegalArgumentException(
                "Key field and values arrays must be of equal length.");
        }
        _pClass = pClass;
        _mField = mField;
        _mValues = mValues;
        _map = new HashMap<String, Comparable<?>>();
        for (int i = 0; i < sFields.length; i ++) {
            _map.put(sFields[i], sValues[i]);
        }
    }

    public Class<T> getPersistentClass ()
    {
        return _pClass;
    }

    public Map<String, Comparable<?>> getSingleFieldsMap ()
    {
        return _map;
    }

    public String getMultiField ()
    {
        return _mField;
    }

    public Comparable<?>[] getMultiValues ()
    {
        return _mValues;
    }

    // from WhereClause
    public SQLExpression getWhereExpression ()
    {
        throw new RuntimeException("Not used");
    }

    // from SQLExpression
    public void accept (ExpressionVisitor builder)
    {
        builder.visit(this);
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        // nothing to add
    }

    // from ValidatingCacheInvalidator
    public void validateFlushType (Class<?> pClass)
    {
        if (!pClass.equals(_pClass)) {
            throw new IllegalArgumentException(
                "Class mismatch between persistent record and cache invalidator " +
                "[record=" + pClass.getSimpleName() +
                ", invtype=" + _pClass.getSimpleName() + "].");
        }
    }

    // from CacheInvalidator
    public void invalidate (PersistenceContext ctx)
    {
        HashMap<String, Comparable<?>> newMap = new HashMap<String, Comparable<?>>(_map);
        for (int i = 0; i < _mValues.length; i ++) {
            newMap.put(_mField, _mValues[i]);
            ctx.cacheInvalidate(new SimpleCacheKey(_pClass, newMap));
        }
    }

    @Override // from WhereClause
    public void validateQueryType (Class<?> pClass)
    {
        super.validateQueryType(pClass);
        validateTypesMatch(pClass, _pClass);
    }

    protected String _mField;
    protected Comparable<?>[] _mValues;
    protected Class<T> _pClass;
    protected HashMap<String, Comparable<?>> _map;
}
