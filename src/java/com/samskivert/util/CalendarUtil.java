//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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
 * Contains some useful calendar related functions.
 */
public class CalendarUtil
{
    /**
     * Set all the time components of the passed in calendar to zero. Returns the calendar for
     * handy chaining.
     */
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
     * Returns the absolute difference between two longs, which have the
     * significance of acting as miliseconds since the Epoch.
     */
    public static long getTimeBetween (long start, long end)
    {
        return Math.abs(start - end);
    }

    /**
     * Returns the difference between the dates represented by the two
     * calendars in days, properly accounting for daylight savings time, leap
     * seconds, etc. The order of the two dates in time does not matter, the
     * absolute number of days between them will be returned.
     *
     * <p> From: http://www.jguru.com/forums/view.jsp?EID=489372
     *
     * @return the number of days between d1 and d2, 0 if they are the
     * same day.
     */
    public static int getDaysBetween (Calendar d1, Calendar d2)
    {
        if (d1.after(d2)) {  // swap dates so that d1 is start and d2 is end
            Calendar swap = d1;
            d1 = d2;
            d2 = swap;
        }

        int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
        int y2 = d2.get(Calendar.YEAR);
        if (d1.get(Calendar.YEAR) != y2) {
            d1 = (Calendar)d1.clone();
            do {
                days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);
                d1.add(Calendar.YEAR, 1);
            } while (d1.get(Calendar.YEAR) != y2);
        }
        return days;
    }

    /**
     * Returns the number of whole months between the dates represented by the
     * two calendar objects, truncating any remainder. The order of the two
     * dates in time does not matter, the absolute number of months between
     * them will be returned.
     */
    public static int getMonthsBetween (Calendar start, Calendar end)
    {
        if (end.before(start)) {
            Calendar swap = start;
            start = end;
            end = swap;
        }

        // we're going to manipulate end, so let's clone it
        end = (Calendar)end.clone();

        int months = -1;
        do {
            end.add(Calendar.MONTH, -1);
            months++;
        } while (!start.after(end));

        return months;
    }
}
