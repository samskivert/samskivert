/**
 * $Id: history.h,v 1.1 2002/03/14 16:45:26 shaper Exp $
 * 
 * lookuplet - a utility for quickly looking up information
 * Copyright (C) 2001 Michael Bayne
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

#ifndef _HISTORY_H_
#define _HISTORY_H_

#include <glib.h>

/**
 * Initializes the history storage.
 */
void
lk_init_history (void);

/**
 * Returns the next history entry if next is true, else returns the
 * previous history entry.
 */
const gchar*
lk_get_history (gboolean next);

/**
 * Returns the most recent history entry that matches the specified
 * partial entry text, or NULL if there are no matches.  String
 * comparisons are case-insensitive as this is intended for use in
 * effecting auto-completion of sloppily-inputted partial entries.  If a
 * matching entry is found, the current history item index is set to point
 * to that entry such that subsequent history entry retrievals will
 * automagically pick up from that point.
 */
const gchar*
lk_get_history_expand (const gchar* entry);

/**
 * Adds an entry to the history and resets the current history item index
 * to point to the new entry.
 */
void
lk_add_history (const gchar* entry);

#endif /* _HISTORY_H_ */
