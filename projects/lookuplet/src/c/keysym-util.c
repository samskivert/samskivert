/**
 * $Id: keysym-util.c,v 1.2 2001/08/16 20:25:09 mdb Exp $
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

#include "keysym-util.h"

static gboolean
string_empty (const char* string)
{
    return (string == NULL ||
	    string[0] == '\0');
}

gboolean
convert_string_to_keysym_state (const char* string,
			        guint* keysym,
				guint* state)
{
    char* s, *p;

    g_return_val_if_fail(keysym != NULL, FALSE);
    g_return_val_if_fail(state != NULL, FALSE);
	
    *state = 0;
    *keysym = 0;

    if (string_empty(string) ||
	strcmp(string, "Disabled") == 0 ||
	strcmp(string, _("Disabled")) == 0) {
	return FALSE;
    }

    s = g_strdup(string);

    gdk_error_trap_push();

    p = strtok(s, "-");

    while (p != NULL) {
	if (strcmp(p, "Control")==0) {
	    *state |= GDK_CONTROL_MASK;
	} else if (strcmp(p, "Lock")==0) {
	    *state |= GDK_LOCK_MASK;
	} else if (strcmp(p, "Shift")==0) {
	    *state |= GDK_SHIFT_MASK;
	} else if (strcmp(p, "Mod1")==0) {
	    *state |= GDK_MOD1_MASK;
	} else if (strcmp(p, "Mod2")==0) {
	    *state |= GDK_MOD2_MASK;
	} else if (strcmp(p, "Mod3")==0) {
	    *state |= GDK_MOD3_MASK;
	} else if (strcmp(p, "Mod4")==0) {
	    *state |= GDK_MOD4_MASK;
	} else if (strcmp(p, "Mod5")==0) {
	    *state |= GDK_MOD5_MASK;
	} else {
	    *keysym = gdk_keyval_from_name(p);
	    if (*keysym == 0) {
		gdk_flush();
		gdk_error_trap_pop();
		g_free(s);
		return FALSE;
	    }
	} 
	p = strtok(NULL, "-");
    }

    gdk_flush();
    gdk_error_trap_pop();
    g_free(s);

    return (*keysym != 0);
}

char*
convert_keysym_state_to_string (guint keysym,
				guint state)
{
    GString* gs;
    char* sep = "";
    char* key;

    if (keysym == 0) {
	return g_strdup(_("Disabled"));
    }

    gdk_error_trap_push();
    key = gdk_keyval_name(keysym);
    gdk_flush();
    gdk_error_trap_pop();

    if (key == NULL) {
	return NULL;
    }

    gs = g_string_new(NULL);

    if (state & GDK_CONTROL_MASK) {
	g_string_append(gs, "Control");
	sep = "-";
    }
    if (state & GDK_LOCK_MASK) {
	g_string_append(gs, sep);
	g_string_append(gs, "Lock");
	sep = "-";
    }
    if (state & GDK_SHIFT_MASK) {
	g_string_append(gs, sep);
	g_string_append(gs, "Shift");
	sep = "-";
    }
    if (state & GDK_MOD1_MASK) {
	g_string_append(gs, sep);
	g_string_append(gs, "Mod1");
	sep = "-";
    }
    if (state & GDK_MOD2_MASK) {
	g_string_append(gs, sep);
	g_string_append(gs, "Mod2");
	sep = "-";
    }
    if (state & GDK_MOD3_MASK) {
	g_string_append(gs, sep);
	g_string_append(gs, "Mod3");
	sep = "-";
    }
    if (state & GDK_MOD4_MASK) {
	g_string_append(gs, sep);
	g_string_append(gs, "Mod4");
	sep = "-";
    }
    if (state & GDK_MOD5_MASK) {
	g_string_append(gs, sep);
	g_string_append(gs, "Mod5");
	sep = "-";
    }

    g_string_append(gs, sep);
    g_string_append(gs, key);

    {
	char *ret = gs->str;
	g_string_free(gs, FALSE);
	return ret;
    }
}
