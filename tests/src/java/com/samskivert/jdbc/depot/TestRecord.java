//
// $Id$

package com.samskivert.jdbc.depot;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Id;

import com.samskivert.util.StringUtil;

/**
 * A test persistent object.
 */
public class TestRecord
{
    @Id
    public int recordId;

    public String name;

    public int age;

    public Date created;

    public Timestamp lastModified;

    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
