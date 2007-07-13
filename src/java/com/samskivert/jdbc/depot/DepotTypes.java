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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.samskivert.io.PersistenceException;

/**
 * Primarily an implementation of {@link QueryBuilderContext}, this class holds the persistent
 * classes used in the context of a single query and maps them to {@link DepotMarshaller}
 * instances as well as table names and table abbreviations.
 * 
 * The main motivation for breaking this functionality out into its own class is to encapsulate
 * the operation that throws {@link PersistenceException} as separate from the operations that
 * throw {@link SQLException}.
 */
public class DepotTypes<T>
    implements QueryBuilderContext<T>
{
    public DepotTypes (PersistenceContext ctx,
                       Class<? extends PersistentRecord> main,
                       Set<Class<? extends PersistentRecord>> others)
        throws PersistenceException
    {
        _mainType = main;
        _classMap = new HashMap<Class, DepotMarshaller>();

        for (Class<? extends PersistentRecord> c : others) {
            _classMap.put(c, ctx.getMarshaller(c));
        }
        _classList = new ArrayList<Class<? extends PersistentRecord>>(others);
    }

    public Class<? extends PersistentRecord> getMainType ()
    {
        return _mainType;
    }

    // from ConstructedQuery
    public String getTableName (Class<? extends PersistentRecord> cl)
    {
        return _classMap.get(cl).getTableName();
    }

    // from ConstructedQuery
    public String getTableAbbreviation (Class<? extends PersistentRecord> cl)
    {
        int ix = _classList.indexOf(cl);
        if (ix < 0) {
            throw new IllegalArgumentException("Unknown persistence class: " + cl);
        }
        return "T" + (ix+1);
    }

    public DepotMarshaller getMarshaller (Class cl)
    {
        return _classMap.get(cl);
    }

    public String getMainTableName ()
    {
        return getTableName(_mainType);
    }

    public DepotMarshaller getMainMarshaller ()
    {
        return getMarshaller(_mainType);
    }

    public String getMainTableAbbreviation ()
    {
        return getTableAbbreviation(_mainType);
    }

    /** The persistent class to instantiate for the results. */
    protected Class<? extends PersistentRecord> _mainType;

    /** A list of referenced classes, used to generate table abbreviations. */
    protected List<Class<? extends PersistentRecord>> _classList;

    /** Classes mapped to marshallers, used for table names and field lists. */
    protected Map<Class, DepotMarshaller> _classMap;
}
