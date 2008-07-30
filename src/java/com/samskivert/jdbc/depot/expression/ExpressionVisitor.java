//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne, Pär Winzell
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

import com.samskivert.jdbc.depot.Key.WhereCondition;
import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.MultiKey;
import com.samskivert.jdbc.depot.PersistentRecord;

import com.samskivert.jdbc.depot.clause.DeleteClause;
import com.samskivert.jdbc.depot.clause.FieldDefinition;
import com.samskivert.jdbc.depot.clause.ForUpdate;
import com.samskivert.jdbc.depot.clause.FromOverride;
import com.samskivert.jdbc.depot.clause.GroupBy;
import com.samskivert.jdbc.depot.clause.InsertClause;
import com.samskivert.jdbc.depot.clause.Join;
import com.samskivert.jdbc.depot.clause.Limit;
import com.samskivert.jdbc.depot.clause.OrderBy;
import com.samskivert.jdbc.depot.clause.SelectClause;
import com.samskivert.jdbc.depot.clause.UpdateClause;
import com.samskivert.jdbc.depot.clause.Where;

import com.samskivert.jdbc.depot.operator.Conditionals.Exists;
import com.samskivert.jdbc.depot.operator.Conditionals.In;
import com.samskivert.jdbc.depot.operator.Conditionals.IsNull;
import com.samskivert.jdbc.depot.operator.Conditionals.FullTextMatch;
import com.samskivert.jdbc.depot.operator.Logic.Not;
import com.samskivert.jdbc.depot.operator.SQLOperator.BinaryOperator;
import com.samskivert.jdbc.depot.operator.SQLOperator.MultiOperator;

/**
 * Enumerates visitation methods for every possible SQL expression type.
 */
public interface ExpressionVisitor
{
    public void visit (FieldDefinition fieldOverride)
        throws Exception;
    public void visit (WhereCondition<? extends PersistentRecord> whereCondition)
        throws Exception;
    public void visit (Key<? extends PersistentRecord> key)
        throws Exception;
    public void visit (MultiKey<? extends PersistentRecord> key)
        throws Exception;
    public void visit (FunctionExp functionExp)
        throws Exception;
    public void visit (EpochSeconds epochSeconds)
        throws Exception;
    public void visit (FromOverride fromOverride)
        throws Exception;
    public void visit (MultiOperator multiOperator)
        throws Exception;
    public void visit (BinaryOperator binaryOperator)
        throws Exception;
    public void visit (IsNull isNull)
        throws Exception;
    public void visit (In in)
        throws Exception;
    public void visit (FullTextMatch match)
        throws Exception;
    public void visit (ColumnExp columnExp)
        throws Exception;
    public void visit (Not not)
        throws Exception;
    public void visit (GroupBy groupBy)
        throws Exception;
    public void visit (ForUpdate forUpdate)
        throws Exception;
    public void visit (OrderBy orderBy)
        throws Exception;
    public void visit (Where where)
        throws Exception;
    public void visit (Join join)
        throws Exception;
    public void visit (Limit limit)
        throws Exception;
    public void visit (LiteralExp literalExp)
        throws Exception;
    public void visit (ValueExp valueExp)
        throws Exception;
    public void visit (Exists<? extends PersistentRecord> exists)
        throws Exception;
    public void visit (SelectClause<? extends PersistentRecord> selectClause)
        throws Exception;
    public void visit (UpdateClause<? extends PersistentRecord> updateClause)
        throws Exception;
    public void visit (DeleteClause<? extends PersistentRecord> deleteClause)
        throws Exception;
    public void visit (InsertClause<? extends PersistentRecord> insertClause)
        throws Exception;
}
