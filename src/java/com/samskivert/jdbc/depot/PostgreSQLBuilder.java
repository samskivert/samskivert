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

package com.samskivert.jdbc.depot;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;

import com.samskivert.jdbc.depot.FieldMarshaller.BooleanMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ByteArrayMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ByteEnumMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ByteMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.DoubleMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.FloatMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.IntArrayMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.IntMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.LongMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ObjectMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ShortMarshaller;

public class PostgreSQLBuilder
    extends SQLBuilder
{
    public class PGBuildVisitor extends BuildVisitor
    {
        protected PGBuildVisitor (DepotTypes types)
        {
            super(types);
        }

        protected void appendTableName (Class<? extends PersistentRecord> type)
        {
            _builder.append("\"").append(_types.getTableName(type)).append("\"");
        }

        protected void appendTableAbbreviation (Class<? extends PersistentRecord> type)
        {
            _builder.append("\"").append(_types.getTableAbbreviation(type)).append("\"");
        }

        protected void appendColumn (Class<? extends PersistentRecord> type, String field)
        {
            _builder.append("\"").append(_types.getColumnName(type, field)).append("\"");
        }

        protected void appendField (String field)
        {
            _builder.append("\"").append(field).append("\"");
        }
    }

    public class PGBindVisitor extends BindVisitor
    {
        protected PGBindVisitor (DepotTypes types, PreparedStatement stmt)
        {
            super(types, stmt);
        }
    }

    public PostgreSQLBuilder (DepotTypes types)
    {
        super(types);
    }

    @Override
    protected BuildVisitor getBuildVisitor ()
    {
        return new PGBuildVisitor(_types);
    }

    @Override
    protected BindVisitor getBindVisitor (PreparedStatement stmt)
    {
        return new PGBindVisitor(_types, stmt);
    }

    @Override
    protected <T> String getColumnType (FieldMarshaller fm)
    {
        if (fm instanceof ByteMarshaller) {
            return "SMALLINT";
        } else if (fm instanceof ShortMarshaller) {
            return "SMALLINT";
        } else if (fm instanceof IntMarshaller) {
            return "INTEGER";
        } else if (fm instanceof LongMarshaller) {
            return "BIGINT";
        } else if (fm instanceof FloatMarshaller) {
            return "FLOAT";
        } else if (fm instanceof DoubleMarshaller) {
            return "DOUBLE";
        } else if (fm instanceof ObjectMarshaller) {
            Class<?> ftype = fm.getField().getType();
            if (ftype.equals(Byte.class)) {
                return "SMALLINT";
            } else if (ftype.equals(Short.class)) {
                return "SMALLINT";
            } else if (ftype.equals(Integer.class)) {
                return "INTEGER";
            } else if (ftype.equals(Long.class)) {
                return "BIGINT";
            } else if (ftype.equals(Float.class)) {
                return "FLOAT";
            } else if (ftype.equals(Double.class)) {
                return "DOUBLE";
            } else if (ftype.equals(String.class)) {
                return "VARCHAR";
            } else if (ftype.equals(Date.class)) {
                return "DATE";
            } else if (ftype.equals(Time.class)) {
                return "TIME";
            } else if (ftype.equals(Timestamp.class)) {
                return "TIMESTAMP";
            } else if (ftype.equals(Blob.class)) {
                return "BYTEA";
            } else if (ftype.equals(Clob.class)) {
                return "TEXT";
            } else {
                throw new IllegalArgumentException(
                    "Don't know how to create SQL for " + ftype + ".");
            }
        } else if (fm instanceof ByteArrayMarshaller) {
            return "BYTEA";
        } else if (fm instanceof IntArrayMarshaller) {
            return "BYTEA";
        } else if (fm instanceof ByteEnumMarshaller) {
            return "SMALLINT";
        } else if (fm instanceof BooleanMarshaller) {
            return "BOOLEAN";
        } else {
            throw new IllegalArgumentException("Unknown field marshaller type: " + fm.getClass());
        }
    }
}
