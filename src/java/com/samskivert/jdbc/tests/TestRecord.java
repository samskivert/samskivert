//
// $Id$

package com.samskivert.jdbc.tests;

import java.sql.Date;
import java.sql.Timestamp;

import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.annotation.Index;

import com.samskivert.util.StringUtil;

/**
 * A test persistent object.
 */
@Entity(indices={ @Index(name="createdIndex", columns={"created"}) })
public class TestRecord
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
