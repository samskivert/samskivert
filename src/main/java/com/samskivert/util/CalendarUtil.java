//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
