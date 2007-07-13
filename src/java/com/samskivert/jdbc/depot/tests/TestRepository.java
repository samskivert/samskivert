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

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.jdbc.depot.DepotRepository;

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
