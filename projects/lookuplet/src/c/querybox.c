/**
 * $Id: querybox.c,v 1.4 2001/03/10 20:30:58 mdb Exp $
 */

#include <config.h>
#include <glib.h>
#include <gnome.h>
#include <ctype.h>
#include <string.h>

#include "binding.h"
#include "launcher.h"
#include "keysym-util.h"
#include "preferences.h"
#include "querybox.h"

static GtkWidget* _query;

static void
selection_received (GtkWidget* widget, GtkSelectionData* selection_data, 
		    gpointer data)
{
    int length = selection_data->length;

    /* make sure some selection was provided */
    if (length > 0) {
        gchar* start, *end;
        gchar* search_text =
            gtk_editable_get_chars(GTK_EDITABLE(widget), 0, -1);

        /* prune spaces from the end of the text */
        for (start = search_text; isspace(*start); start++);
        for (end = search_text + length - 1; isspace(*end); end--);
        end++; *end = '\0';

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
key_pressed (GtkWidget* widget, GdkEvent* event, gpointer callback_data)
{
    GdkEventKey* ek = (GdkEventKey*)event;
    GPtrArray* bindings;
    gint handled = FALSE, i;
    gchar* keystr;

    /* ignore plain or shifted keystrokes */
    if (ek->state <= 1) {
	return FALSE;
    }

    /* otherwise convert the key combo to a name and look it up */
    keystr = convert_keysym_state_to_string(ek->keyval, ek->state);
    bindings = lk_prefs_get_bindings();

    for (i = 0; i < bindings->len; i++) {
	LkBinding* binding = LK_BINDING(g_ptr_array_index(bindings, i));
	if (!strcmp(keystr, binding->key)) {
	    /* get the terms from the query box */
	    gchar* search_text =
		gtk_editable_get_chars(GTK_EDITABLE(widget), 0, -1);
	    /* launch the appropriate thing */
	    lk_launcher_launch(binding, search_text);
	    g_free(search_text);
	    /* clear out the contents of the entry box */
	    gtk_entry_set_text(GTK_ENTRY(widget), "");
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
    g_free(search_text);

    /* clear out the contents of the entry box */
    gtk_entry_set_text(GTK_ENTRY(widget), "");
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

    /* create our preferences button */
    prefs = gtk_button_new_with_label(_("Prefs"));
    gtk_signal_connect (GTK_OBJECT(prefs), "clicked",
			GTK_SIGNAL_FUNC(prefs_clicked), NULL);

    /* create a vertical box to hold our query input box and the label */
    hbox = gtk_hbox_new(FALSE, GNOME_PAD_SMALL);

    /*	pack the query box and label into the hbox */
    gtk_box_pack_start(GTK_BOX(hbox), label, FALSE, FALSE, 0);
    gtk_box_pack_start(GTK_BOX(hbox), _query, TRUE, TRUE, 0);
    gtk_box_pack_start(GTK_BOX(hbox), prefs, FALSE, FALSE, 0);

    return hbox;
}

void
lk_querybox_init (void)
{
    static GdkAtom targets_atom = GDK_NONE;

    /* wire up our key press event handler */
    gtk_signal_connect(GTK_OBJECT(_query), "key_press_event",
		       GTK_SIGNAL_FUNC(key_pressed), NULL);

    /* focus the text entry box */
    gtk_widget_grab_focus(_query);

    /* register a signal handler that will select the selection when it
       arrives */
    gtk_signal_connect_after(GTK_OBJECT(_query), "selection_received",
			     GTK_SIGNAL_FUNC(selection_received), NULL);

    /* request the selection as type "STRING" */
    if (targets_atom == GDK_NONE) {
	targets_atom = gdk_atom_intern ("STRING", FALSE);
    }
    gtk_selection_convert(_query, GDK_SELECTION_PRIMARY, targets_atom,
			  GDK_CURRENT_TIME);
}
