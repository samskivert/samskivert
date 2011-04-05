//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

import java.util.Calendar;

/**
 * @deprecated Use {@link Calendars}.
 */
@Deprecated
public class CalendarUtil
{
    /**
     * @deprecated Use {@link Calendars}.
     */
    @Deprecated
    public static Calendar zeroTime (Calendar cal)
    {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * @deprecated Just use {@link Math#abs}. Duh.
     */
    @Deprecated
    public static long getTimeBetween (long start, long end)
    {
        return Math.abs(start - end);
    }

    /**
     * @deprecated Use {@link Calendars#getDaysBetween}.
     */
    @Deprecated
    public static int getDaysBetween (Calendar d1, Calendar d2)
    {
        return Calendars.getDaysBetween(d1, d2);
    }

    /**
     * @deprecated Use {@link Calendars#getMonthsBetween}.
     */
    @Deprecated
    public static int getMonthsBetween (Calendar start, Calendar end)
    {
        return Calendars.getMonthsBetween(start, end);
    }
}
