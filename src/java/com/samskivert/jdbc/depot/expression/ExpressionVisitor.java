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

import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.MultiKey;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.WhereClause;

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
    public void visit (FieldDefinition fieldOverride);
    public void visit (Key.WhereCondition<? extends PersistentRecord> whereCondition);
    public void visit (MultiKey<? extends PersistentRecord> key);
    public void visit (FunctionExp functionExp);
    public void visit (EpochSeconds epochSeconds);
    public void visit (FromOverride fromOverride);
    public void visit (MultiOperator multiOperator);
    public void visit (BinaryOperator binaryOperator);
    public void visit (IsNull isNull);
    public void visit (In in);
    public void visit (FullTextMatch match);
    public void visit (ColumnExp columnExp);
    public void visit (Not not);
    public void visit (GroupBy groupBy);
    public void visit (ForUpdate forUpdate);
    public void visit (OrderBy orderBy);
    public void visit (WhereClause where);
    public void visit (Join join);
    public void visit (Limit limit);
    public void visit (LiteralExp literalExp);
    public void visit (ValueExp valueExp);
    public void visit (Exists<? extends PersistentRecord> exists);
    public void visit (SelectClause<? extends PersistentRecord> selectClause);
    public void visit (UpdateClause<? extends PersistentRecord> updateClause);
    public void visit (DeleteClause<? extends PersistentRecord> deleteClause);
    public void visit (InsertClause<? extends PersistentRecord> insertClause);
}
