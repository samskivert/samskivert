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
import java.util.Set;

// import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.RandomUtil;
import com.samskivert.jdbc.depot.DepotRepository;
// import com.samskivert.jdbc.depot.EntityMigration;
import com.samskivert.jdbc.depot.PersistenceContext;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.expression.LiteralExp;
import com.samskivert.jdbc.depot.operator.Conditionals;
import com.samskivert.jdbc.depot.clause.Where;

/**
 * A test tool for the Depot repository services.
 */
public class TestRepository extends DepotRepository
{
    public static void main (String[] args)
        throws Exception
    {
        PersistenceContext perCtx = new PersistenceContext(
            "test", new StaticConnectionProvider("depot.properties"));

// tests a bogus rename migration
//         perCtx.registerMigration(TestRecord.class, new EntityMigration.Rename(1, "foo", "bar"));

        TestRepository repo = new TestRepository(perCtx);

        repo.delete(TestRecord.class, 0);

        Date now = new Date(System.currentTimeMillis());
        Timestamp tnow = new Timestamp(System.currentTimeMillis());

        TestRecord record = new TestRecord();
        record.name = "Elvis";
        record.age = 99;
        record.created = now;
        record.lastModified = tnow;

        repo.insert(record);
        System.out.println(repo.load(TestRecord.class, record.recordId));

        record.age = 25;
        record.name = "Bob";
        repo.update(record, "age");
        System.out.println(repo.load(TestRecord.class, record.recordId));

        for (int ii = 1; ii < CREATE_RECORDS; ii++) {
            record = new TestRecord();
            record.recordId = ii;
            record.name = "Spam!";
            record.age = RandomUtil.getInt(150);
            record.created = now;
            record.lastModified = tnow;
            repo.insert(record);
        }

        System.out.println("Have " + repo.findAll(TestRecord.class).size() + " records.");
        repo.deleteAll(TestRecord.class, new Where(new Conditionals.LessThan(
                                                       TestRecord.RECORD_ID_C, CREATE_RECORDS/2)));
        System.out.println("Now have " + repo.findAll(TestRecord.class).size() + " records.");
        repo.deleteAll(TestRecord.class, new Where(new LiteralExp("true")));
        System.out.println("Now have " + repo.findAll(TestRecord.class).size() + " records.");
    }

    public TestRepository (PersistenceContext perCtx)
    {
        super(perCtx);
    }

    @Override // from DepotRepository
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(TestRecord.class);
    }

    protected static final int CREATE_RECORDS = 150;
}
