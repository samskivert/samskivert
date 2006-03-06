//
// $Id: CalendarUtil.java,v 1.2 2004/06/09 09:40:04 mdb Exp $

package com.samskivert.util;

import java.util.Calendar;

/**
 * Contains some useful calendar related functions.
 */
public class CalendarUtil
{
    /**
     * Set all the time components of the passed in calendar to zero.
     */
    public static void zeroTime (Calendar cal)
    {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
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
        } while (!end.after(start));

        return months;
    }
}
