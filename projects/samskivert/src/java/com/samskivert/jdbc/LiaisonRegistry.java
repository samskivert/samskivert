//
// $Id: LiaisonRegistry.java,v 1.2 2001/09/20 02:09:09 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

package com.samskivert.jdbc;

import java.sql.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import com.samskivert.Log;

/**
 * The liaison registry provides access to the appropriate database
 * liaison implementation for a particular database connection.
 */
public class LiaisonRegistry
{
    /**
     * Fetch the appropriate database liaison for the supplied database
     * connection.
     */
    public static DatabaseLiaison getLiaison (Connection conn)
        throws SQLException
    {
        DatabaseMetaData dmd = conn.getMetaData();
        String url = dmd.getURL();

        // see if we already have a liaison mapped for this connection
        DatabaseLiaison liaison = (DatabaseLiaison)_mappings.get(url);

        if (liaison == null) {
            // scan the list looking for a matching liaison
            Iterator iter = _liaisons.iterator();
            while (iter.hasNext()) {
                DatabaseLiaison candidate = (DatabaseLiaison)iter.next();
                if (candidate.matchesURL(url)) {
                    liaison = candidate;
                    break;
                }
            }

            // if we didn't find a matching liaison, use the default
            if (liaison == null) {
                liaison = new DefaultLiaison();
            }

            // map this URL to this liaison
            _mappings.put(url, liaison);
        }

        return liaison;
    }

    protected static void registerLiaisonClass (Class lclass)
    {
        // create a new instance and stick it on our list
        try {
            _liaisons.add(lclass.newInstance());
        } catch (Exception e) {
            Log.warning("Unable to instantiate liaison " +
                        "[class=" + lclass.getName() + ", error=" + e + "].");
        }
    }

    protected static ArrayList _liaisons = new ArrayList();
    protected static HashMap _mappings = new HashMap();
}
