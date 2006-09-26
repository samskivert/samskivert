//
// $Id$

package com.samskivert.jdbc.depot;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Id;
import javax.persistence.Column;

import com.samskivert.util.StringUtil;

/**
 * A test persistent object.
 */
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
