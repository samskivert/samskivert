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

import java.sql.Timestamp;

import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.expression.ColumnExp;

/**
 * Maintains a record of all successfully invoked data migrations.
 */
public class DepotMigrationHistoryRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    /** The column identifier for the {@link #ident} field. */
    public static final String IDENT = "ident";

    /** The qualified column identifier for the {@link #ident} field. */
    public static final ColumnExp IDENT_C =
        new ColumnExp(DepotMigrationHistoryRecord.class, IDENT);

    /** The column identifier for the {@link #whenCompleted} field. */
    public static final String WHEN_COMPLETED = "whenCompleted";

    /** The qualified column identifier for the {@link #whenCompleted} field. */
    public static final ColumnExp WHEN_COMPLETED_C =
        new ColumnExp(DepotMigrationHistoryRecord.class, WHEN_COMPLETED);
    // AUTO-GENERATED: FIELDS END

    /** Our schema version. Probably not likely to change. */
    public static final int SCHEMA_VERSION = 1;

    /** The unique identifier for this migration. */
    @Id public String ident;

    /** The time at which the migration was completed. */
    @Column(nullable=true)
    public Timestamp whenCompleted;

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link DepotMigrationHistoryRecord}
     * with the supplied key values.
     */
    public static Key<DepotMigrationHistoryRecord> getKey (String ident)
    {
        return new Key<DepotMigrationHistoryRecord>(
                DepotMigrationHistoryRecord.class,
                new String[] { IDENT },
                new Comparable[] { ident });
    }
    // AUTO-GENERATED: METHODS END
}
