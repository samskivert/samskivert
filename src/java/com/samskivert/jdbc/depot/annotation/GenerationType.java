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

package com.samskivert.jdbc.depot.annotation;

/**
 * Defines the types of primary key generation.
 */
public enum GenerationType
{
    /**
     * Indicates that the persistence provider must assign primary keys for the entity using an
     * underlying database table to ensure uniqueness.
     */
    TABLE,

    /**
     * Indicates that the persistence provider must assign primary keys for the entity using
     * database sequences.
     */
    SEQUENCE,

    /**
     * Indicates that the persistence provider must assign primary keys for the entity using
     * database identity column.
     */
    IDENTITY,

    /**
     * Indicates that the persistence provider should pick an appropriate strategy for the
     * particular database.
     */
    AUTO;
}
