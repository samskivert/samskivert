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

import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 * Currently only exists as a type without any functionality of its own.
 */
public abstract class WhereClause extends QueryClause
{
    /**
     * Returns the condition associated with this where clause.
     */
    public abstract SQLExpression getWhereExpression ();

    /**
     * Validates that the supplied persistent record type is the type matched by this where clause.
     * Not all clauses will be able to perform this validation, but those that can, should do so to
     * help alleviate programmer error.
     *
     * @exception IllegalArgumentException thrown if the supplied class is known not to by the type
     * matched by this where clause.
     */
    public void validateQueryType (Class<?> pClass)
    {
        // nothing by default
    }

    /**
     * A helper function for implementing {@link #validateQueryType}.
     */
    protected void validateTypesMatch (Class<?> qClass, Class<?> kClass)
    {
        if (!qClass.equals(kClass)) {
            throw new IllegalArgumentException(
                "Class mismatch between persistent record and key in query " +
                "[qtype=" + qClass.getSimpleName() + ", ktype=" + kClass.getSimpleName() + "].");
        }
    }
}
