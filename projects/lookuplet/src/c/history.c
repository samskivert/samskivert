/**
 * $Id: history.c,v 1.1 2002/03/14 16:45:26 shaper Exp $
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

#include <gnome.h>

#include "history.h"

/* the array of historical entries */
static gchar** _history;

/* the last entry index retrieved */
static int _ridx = 0;

/* the insertion index for new history items */
static int _iidx = 0;

/* the number of entries saved in the text entry history */ 
#define HISTORY_COUNT (10)

void
lk_init_history (void)
{
    /* allocate and initialize the array of history entries */
    size_t size = sizeof(gchar*) * HISTORY_COUNT;
    _history = g_malloc(size);
    memset(_history, (int)NULL, size);
}

const gchar*
lk_get_history (gboolean next)
{
    int actidx;

    /* make sure the array is available */
    if (_history == NULL) {
        return NULL;
    }

    /* determine the sought-after index */
    actidx = _ridx + ((next) ? -1 : 1);

    /* make sure the index is valid and an entry exists */
    if (actidx < 0 || actidx >= HISTORY_COUNT || _history[actidx] == NULL) {
        return NULL;
    }

    /* update the retrieved index */
    _ridx = actidx;

    return _history[actidx];
}

const gchar*
lk_get_history_expand (const gchar* entry)
{
    int i, len;

    if (_history == NULL || entry == NULL) {
        return NULL;
    }

    len = strlen(entry);
    for (i = _iidx; i >= 0; i--) {
        gchar* str = _history[i];
        if (str != NULL && !g_strncasecmp(str, entry, len)) {
            /* update the retrieved index for potential subsequent retrievals */
            _ridx = i;
            return str;
        }
    }

    return NULL;
}

void
lk_add_history (const gchar* entry)
{
    /* make sure the array is available and the entry is valid */
    if (_history == NULL || entry == NULL) {
        return;
    }

    /* if there's no space remaining, shift off the oldest entry */
    if (_iidx == HISTORY_COUNT - 1 && _history[_iidx] != NULL) {
        g_free(_history[0]);
        memmove(_history, _history + 1, sizeof(gchar*) * (HISTORY_COUNT - 1));
        _history[_iidx] = NULL;
    }

    /* remember the new entry */
    _history[_iidx] = g_strdup(entry);

    /* increment the insertion and retrieved index */
    if (_iidx < HISTORY_COUNT - 1) {
        _iidx++;
        _ridx = _iidx;
    } else {
        _ridx = HISTORY_COUNT;
    }
}
