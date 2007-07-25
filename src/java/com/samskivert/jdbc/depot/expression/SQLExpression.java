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

package com.samskivert.jdbc.depot.expression;

import java.util.Collection;

import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.SQLBuilder;

/**
 * Represents an SQL expression, e.g. column name, function, or constant.
 */
public interface SQLExpression
{
    /**
     * Most uses of this class have been implemented with a visitor pattern. Create your own
     * {@link ExpressionVisitor} and call this method with it.
     * 
     * @see SQLBuilder
     */
    public void accept (ExpressionVisitor builder)
        throws Exception;

    /**
     * Adds all persistent classes that are brought into the SQL context by this clause: FROM
     * clauses, JOINs, UPDATEs, anything that could create a new table abbreviation. This method
     * should recurse into any subordinate state that may in turn bring in new classes so that
     * sub-queries work correctly.
     */
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet);
}
