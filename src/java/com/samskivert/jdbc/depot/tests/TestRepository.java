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
// import java.util.HashSet;
import java.util.Set;

import com.samskivert.util.RandomUtil;

import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.jdbc.depot.DepotRepository;
// import com.samskivert.jdbc.depot.Key;
// import com.samskivert.jdbc.depot.KeySet;
import com.samskivert.jdbc.depot.PersistenceContext;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.SchemaMigration;
import com.samskivert.jdbc.depot.clause.Where;
import com.samskivert.jdbc.depot.expression.LiteralExp;
import com.samskivert.jdbc.depot.operator.Conditionals;

/**
 * A test tool for the Depot repository services.
 */
public class TestRepository extends DepotRepository
{
    public static void main (String[] args)
        throws Exception
    {
        PersistenceContext perCtx = new PersistenceContext();
        perCtx.init("test", new StaticConnectionProvider("depot.properties"), null);

        // tests a bogus rename migration
        // perCtx.registerMigration(TestRecord.class, new SchemaMigration.Rename(1, "foo", "bar"));

        // tests a custom add column migration
        perCtx.registerMigration(TestRecord.class,
                                 new SchemaMigration.Add(2, TestRecord.HOME_TOWN, "'Anytown USA'"));

        TestRepository repo = new TestRepository(perCtx);

        repo.delete(TestRecord.class, 1);

        Date now = new Date(System.currentTimeMillis());
        Timestamp tnow = new Timestamp(System.currentTimeMillis());

        TestRecord record = new TestRecord();
        record.recordId = 1;
        record.name = "Elvis";
        record.age = 99;
        record.created = now;
        record.homeTown = "Right here";
        record.lastModified = tnow;
        record.numbers = new int[] { 9, 0, 2, 1, 0 };

        repo.insert(record);
        System.out.println(repo.load(TestRecord.class, record.recordId));

//         record.age = 25;
//         record.name = "Bob";
//         record.numbers = new int[] { 1, 2, 3, 4, 5 };
//         repo.update(record, TestRecord.AGE, TestRecord.NAME, TestRecord.NUMBERS);

        repo.updatePartial(TestRecord.class, record.recordId,
                           TestRecord.AGE, 25, TestRecord.NAME, "Bob",
                           TestRecord.NUMBERS, new int[] { 1, 2, 3, 4, 5 });
        System.out.println(repo.load(TestRecord.class, record.recordId));

        for (int ii = 2; ii < CREATE_RECORDS; ii++) {
            record = new TestRecord();
            record.recordId = ii;
            record.name = "Spam!";
            record.age = RandomUtil.getInt(150);
            record.homeTown = "Over there";
            record.numbers = new int[] { 5, 4, 3, 2, 1 };
            record.created = now;
            record.lastModified = tnow;
            repo.insert(record);
        }

        System.out.println("Have " + repo.findAll(TestRecord.class).size() + " records.");
        repo.deleteAll(TestRecord.class, new Where(new Conditionals.LessThan(
                                                       TestRecord.RECORD_ID_C, CREATE_RECORDS/2)));
        System.out.println("Now have " + repo.findAll(TestRecord.class).size() + " records.");
        repo.deleteAll(TestRecord.class, new Where(new LiteralExp("true")));
//         // TODO: try to break our In() clause
//         Set<Key<TestRecord>> ids = new HashSet<Key<TestRecord>>();
//         for (int ii = 1; ii <= Conditionals.In.MAX_KEYS*2+3; ii++) {
//             ids.add(TestRecord.getKey(ii));
//         }
//         repo.deleteAll(TestRecord.class, new KeySet<TestRecord>(TestRecord.class, ids));
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
