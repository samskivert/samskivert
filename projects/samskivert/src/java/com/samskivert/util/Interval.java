//
// $Id: Interval.java,v 1.4 2002/05/23 23:43:17 ray Exp $
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

package com.samskivert.util;

import java.util.*;

/**
 * An interface for doing operations after some delay. Normally,
 * <code>intervalExpired()</code> should not do anything that will take
 * very long.
 *
 * @see IntervalManager
 */
public interface Interval
{
    public void intervalExpired (int id, Object arg);
}
