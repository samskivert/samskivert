/**
 * $Id: querybox.c,v 1.9 2002/03/14 16:45:26 shaper Exp $
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

#include <config.h>
#include <glib.h>
#include <gnome.h>
#include <ctype.h>
#include <string.h>

#include "binding.h"
#include "launcher.h"
#include "history.h"
#include "keysym-util.h"
#include "preferences.h"
#include "lookuplet.h"
#include "querybox.h"

/* the query text entry field */
static GtkWidget* _query;

/* used by url_p */
#define MATCH_PREFIX(text, prefix) !strncmp(text, prefix, sizeof(prefix)-1)

/* key codes for navigating the entry history */
#define NEXT_HISTORY_KEY (GDK_Up)
#define PREVIOUS_HISTORY_KEY (GDK_Down)

/* key code for auto-completion of a partial entry */
#define AUTO_COMPLETE_KEY (GDK_Tab)

/**
 * A very primitive function to try to determine if the selection is a URL
 * of some sort.
 */
static int
url_p (const char* text)
{
    return (MATCH_PREFIX(text, "http://") ||
            MATCH_PREFIX(text, "ftp://") ||
            MATCH_PREFIX(text, "https://") ||
            MATCH_PREFIX(text, "file:/"));
}

static void
selection_received (GtkWidget* widget, GtkSelectionData* selection_data, 
		    gpointer data)
{
    int length = selection_data->length;
    int is_url;

    /* make sure some selection was provided */
    if (length > 0) {
        gchar* start, *end, *pos;
        gchar* search_text =
            gtk_editable_get_chars(GTK_EDITABLE(widget), 0, -1);

        /* prune spaces from the end of the text */
        for (start = search_text; isspace(*start); start++);
        for (end = search_text + length - 1; isspace(*end); end--);
        end++; *end = '\0';

        /* check to see if we're looking at a url here */
        is_url = url_p(start);

        /* do some whitespace processing */
        for (pos = start; *pos != '\0'; pos++) {
            /* convert newlines to spaces */
            if ((*pos == '\n') || (*pos == '\r')) {
                *pos = ' ';
            }
            /* if this is a URL, we want to eat whitespace */
            if (*pos == ' ' && is_url) {
                memmove(pos, pos+1, strlen(pos));
            }
        }

        /* only set the new text if we changed anything */
        if (length != strlen(start)) {
            length = strlen(start);
            gtk_entry_set_text(GTK_ENTRY(widget), start);
        }
        g_free(search_text);

	/* select the text in the entry so that it can be easily replaced */
	gtk_editable_select_region(GTK_EDITABLE(_query), 0, length);
    }
}

static gint
handle_special_keys (GtkWidget* widget, GdkEventKey* ek)
{
    const gchar* entry = NULL;

    switch (ek->keyval) {
    case NEXT_HISTORY_KEY:
    case PREVIOUS_HISTORY_KEY:
        /* retrieve the sought-after historical entry */
        entry = lk_get_history(ek->keyval == NEXT_HISTORY_KEY);
        break;

    case AUTO_COMPLETE_KEY: {
        /* get the terms from the query box */
        gchar* text = gtk_editable_get_chars(GTK_EDITABLE(widget), 0, -1);
        /* seek an expanded historical entry for the current text */
        entry = lk_get_history_expand(text);
        }
        break;
    }

    /* if we've an entry, fill it in and we're done */
    if (entry != NULL) {
        /* place the entry in the text field */
        gtk_entry_set_text(GTK_ENTRY(widget), entry);

        /* select the text in the entry so that it can be easily replaced */
        gtk_editable_select_region(GTK_EDITABLE(_query), 0, strlen(entry));

        return TRUE;
    }

    return FALSE;
}

static gint
key_pressed (GtkWidget* widget, GdkEvent* event, gpointer callback_data)
{
    GdkEventKey* ek = (GdkEventKey*)event;
    GPtrArray* bindings;
    gint handled = FALSE, i;
    gchar* keystr;

    /* handle special keystrokes such as history and auto-complete */
    if (handle_special_keys(widget, ek)) {
        return TRUE;
    }

    /* ignore plain or shifted keystrokes */
    if (ek->state <= 1) {
	return FALSE;
    }

    /* otherwise convert the key combo to a name and look it up */
    keystr = convert_keysym_state_to_string(ek->keyval, ek->state);
    if (keystr == NULL) {
        return FALSE;
    }
    bindings = lk_prefs_get_bindings();

    for (i = 0; i < bindings->len; i++) {
	LkBinding* binding = LK_BINDING(g_ptr_array_index(bindings, i));
	if (!strcmp(keystr, binding->key)) {
	    /* get the terms from the query box */
	    gchar* search_text =
		gtk_editable_get_chars(GTK_EDITABLE(widget), 0, -1);
            /* save this entry in the history */
            lk_add_history(search_text);
	    /* launch the appropriate thing */
	    lk_launcher_launch(binding, search_text);
            g_free(search_text);
            if (applet_mode) {
                /* clear out the contents of the entry box */
                gtk_entry_set_text(GTK_ENTRY(widget), "");
            } else {
                gtk_exit(0);
            }
	    handled = TRUE;
	    break;
	}
    }

    g_free(keystr);

    return handled;
}

static void
prefs_clicked (GtkWidget* widget, gpointer data)
{
    lk_prefs_display();
}

static void
launch (GtkWidget* widget, gpointer data)
{
    /* pass the contents of the querybox on to the launcher for display
     * (specifying NULL for the qualifier since they just pressed return
     * instead of pressing some special key combination) */
    gchar* search_text = gtk_editable_get_chars(GTK_EDITABLE(widget), 0, -1);
    lk_launcher_launch(NULL, search_text);

    if (applet_mode) {
        /* save this entry in the history */
        lk_add_history(search_text);
        g_free(search_text);
        /* clear out the contents of the entry box */
        gtk_entry_set_text(GTK_ENTRY(widget), "");
    } else {
        gtk_exit(0);
    }
}

GtkWidget*
lk_querybox_create (void)
{
    GtkWidget* hbox;
    GtkWidget* label;
    GtkWidget* prefs;

    /* create the widget we are going to put on the applet */
    label = gtk_label_new(_("Query"));

    /* make the query text box and attach the "activate" signal handler */
    _query = gtk_entry_new();

    /* Connect "return" in the search box to the launch func. */
    gtk_signal_connect(GTK_OBJECT(_query), "activate",
		       GTK_SIGNAL_FUNC(launch), NULL);

    /* create a horizontal box to hold our query input box and the
     * label */
    hbox = gtk_hbox_new(FALSE, GNOME_PAD_SMALL);

    /*	pack the query box and label into the hbox */
    gtk_box_pack_start(GTK_BOX(hbox), label, FALSE, FALSE, 0);
    gtk_box_pack_start(GTK_BOX(hbox), _query, TRUE, TRUE, 0);

    /* create our preferences button (only if we're not an applet, if
     * we're an applet we'll have a preferences menu item instead) */
    if (!applet_mode) {
        prefs = gtk_button_new_with_label(_("Prefs"));
        gtk_signal_connect (GTK_OBJECT(prefs), "clicked",
                            GTK_SIGNAL_FUNC(prefs_clicked), NULL);
        gtk_box_pack_start(GTK_BOX(hbox), prefs, FALSE, FALSE, 0);
    }

    /* create the array of history commands */
    lk_init_history();

    return hbox;
}

void
lk_querybox_init (void)
{
    static GdkAtom targets_atom = GDK_NONE;

    /* wire up our key press event handler */
    gtk_signal_connect(GTK_OBJECT(_query), "key_press_event",
		       GTK_SIGNAL_FUNC(key_pressed), NULL);

    /* if we're not an applet, we want to request focus and grab the
       current X selection */
    if (!applet_mode) {
        /* focus the text entry box */
        gtk_widget_grab_focus(_query);

        /* register a signal handler that will select the selection when
           it arrives */
        gtk_signal_connect_after(GTK_OBJECT(_query), "selection_received",
                                 GTK_SIGNAL_FUNC(selection_received), NULL);

        /* request the selection as type "STRING" */
        if (targets_atom == GDK_NONE) {
            targets_atom = gdk_atom_intern ("STRING", FALSE);
        }
        gtk_selection_convert(_query, GDK_SELECTION_PRIMARY, targets_atom,
                              GDK_CURRENT_TIME);
    }
}
