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

import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.annotation.Index;

import com.samskivert.util.StringUtil;

/**
 * A test persistent object.
 */
@Entity(indices={ @Index(name="createdIndex", fields={"created"}) })
public class TestRecord extends PersistentRecord
{
    public static final int SCHEMA_VERSION = 1;

    @Id
    public int recordId;

    @Column(nullable=false)
    public String name;

    @Column(nullable=false)
    public int age;

    @Column(nullable=false)
    public Date created;

    @Column(nullable=false)
    public Timestamp lastModified;

    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
