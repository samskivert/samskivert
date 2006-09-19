//
// $Id$

package com.samskivert.jdbc.depot;

import java.sql.Date;
import java.sql.Timestamp;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;

/**
 * A test tool for the Depot repository services.
 */
public class TestRepository extends DepotRepository
{
    public static void main (String[] args)
        throws Exception
    {
        TestRepository repo = new TestRepository(
            new StaticConnectionProvider("depot.properties"));

        repo.delete(TestRecord.class, 0);

        TestRecord record = new TestRecord();
        record.name = "Elvis";
        record.age = 99;
        record.created = new Date(System.currentTimeMillis());
        record.lastModified = new Timestamp(System.currentTimeMillis());

        repo.insert(record);
        System.out.println(repo.load(TestRecord.class, record.recordId));

        record.age = 25;
        record.name = "Bob";
        repo.update(record, "age");
        System.out.println(repo.load(TestRecord.class, record.recordId));
    }

    public TestRepository (ConnectionProvider conprov)
    {
        super(conprov);
    }
}
