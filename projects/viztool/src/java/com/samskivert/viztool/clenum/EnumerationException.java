//
// $Id: EnumerationException.java,v 1.2 2001/08/12 04:36:57 mdb Exp $
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.viztool.clenum;

/**
 * An enumeration exception is thrown when some problem occurs while
 * attempting to enumerate over a classpath component. This may be when
 * initially attempting to read a zip or jar file, or during the process
 * of enumeration.
 */
public class EnumerationException extends Exception
{
    public EnumerationException (String message)
    {
        super(message);
    }
}
