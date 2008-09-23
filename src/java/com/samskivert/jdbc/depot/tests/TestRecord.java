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

package com.samskivert.jdbc.depot.tests;

import java.sql.Date;
import java.sql.Timestamp;

import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.annotation.Index;
import com.samskivert.jdbc.depot.expression.ColumnExp;

import com.samskivert.util.StringUtil;

/**
 * A test persistent object.
 */
@Entity(indices={ @Index(name="createdIndex", fields={"created"}) })
public class TestRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    /** The column identifier for the {@link #recordId} field. */
    public static final String RECORD_ID = "recordId";

    /** The qualified column identifier for the {@link #recordId} field. */
    public static final ColumnExp RECORD_ID_C =
        new ColumnExp(TestRecord.class, RECORD_ID);

    /** The column identifier for the {@link #name} field. */
    public static final String NAME = "name";

    /** The qualified column identifier for the {@link #name} field. */
    public static final ColumnExp NAME_C =
        new ColumnExp(TestRecord.class, NAME);

    /** The column identifier for the {@link #age} field. */
    public static final String AGE = "age";

    /** The qualified column identifier for the {@link #age} field. */
    public static final ColumnExp AGE_C =
        new ColumnExp(TestRecord.class, AGE);

    /** The column identifier for the {@link #homeTown} field. */
    public static final String HOME_TOWN = "homeTown";

    /** The qualified column identifier for the {@link #homeTown} field. */
    public static final ColumnExp HOME_TOWN_C =
        new ColumnExp(TestRecord.class, HOME_TOWN);

    /** The column identifier for the {@link #created} field. */
    public static final String CREATED = "created";

    /** The qualified column identifier for the {@link #created} field. */
    public static final ColumnExp CREATED_C =
        new ColumnExp(TestRecord.class, CREATED);

    /** The column identifier for the {@link #lastModified} field. */
    public static final String LAST_MODIFIED = "lastModified";

    /** The qualified column identifier for the {@link #lastModified} field. */
    public static final ColumnExp LAST_MODIFIED_C =
        new ColumnExp(TestRecord.class, LAST_MODIFIED);

    /** The column identifier for the {@link #numbers} field. */
    public static final String NUMBERS = "numbers";

    /** The qualified column identifier for the {@link #numbers} field. */
    public static final ColumnExp NUMBERS_C =
        new ColumnExp(TestRecord.class, NUMBERS);
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 3;

    @Id
    public int recordId;

    public String name;

    public int age;

    public String homeTown;

    public Date created;

    public Timestamp lastModified;

    public int[] numbers;

    @Override
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link TestRecord}
     * with the supplied key values.
     */
    public static Key<TestRecord> getKey (int recordId)
    {
        return new Key<TestRecord>(
                TestRecord.class,
                new String[] { RECORD_ID },
                new Comparable[] { recordId });
    }
    // AUTO-GENERATED: METHODS END
}
