//
// $Id: CalendarUtil.java,v 1.1 2003/10/22 00:58:47 eric Exp $

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
        cal.clear(Calendar.HOUR);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
    }
}
