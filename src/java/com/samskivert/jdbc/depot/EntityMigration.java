//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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

package com.samskivert.jdbc.depot;

/**
 * These can be registered with the {@link PersistenceContext} to effect hand-coded migrations
 * between entity versions. The modifier should override {@link #invoke} to perform its
 * migrations. See {@link PersistenceContext#registerPreMigration} and {@link
 * PersistenceContext#registerPostMigration}.
 */
public abstract class EntityMigration extends Modifier
{
    /**
     * If this method returns true, this migration will be run <b>before</b> the default
     * migrations, if false it will be run after.
     */
    public boolean runBeforeDefault ()
    {
        return true;
    }

    /**
     * When an Entity is being migrated, this method will be called to check whether this migration
     * should be run.
     */
    public abstract boolean shouldRunMigration (int currentVersion, int targetVersion);

    protected EntityMigration ()
    {
        super(null);
    }
}
