/**
 * $Id: querybox.c,v 1.1 2000/12/10 23:38:39 mdb Exp $
 */

#include <config.h>
#include <glib.h>
#include <gnome.h>

#include "launcher.h"
#include "querybox.h"

#define BORDER 1
#define SPACER 2

static void
launch (GtkWidget* widget, gpointer data)
{
    gchar* search_text = gtk_entry_get_text(GTK_ENTRY(widget));

    /* pass the contents of the querybox on to the launcher for display
     * (specifying NULL for the qualifier since they just pressed return
     * instead of pressing some special key combination) */
    lookuplet_launcher_launch(search_text, NULL);

    /* clear out the contents of the entry box */
    gtk_entry_set_text(GTK_ENTRY(widget), "");
}

GtkWidget*
lookuplet_querybox_create ()
{
    GtkWidget* vbox;
    GtkWidget* label;
    GtkWidget* query;

    /* create the widget we are going to put on the applet */
    label = gtk_label_new(_("Query"));
    gtk_widget_show(label);

    /* make the query text box and attach the "activate" signal handler */
    query = gtk_entry_new();

    /* Connect "return" in the search box to the launch func. */
    gtk_signal_connect(GTK_OBJECT(query), "activate",
		       GTK_SIGNAL_FUNC(launch), NULL);

    /* create a vertical box to hold our query input box and the label */
    vbox = gtk_hbox_new(FALSE, SPACER);

    /*	pack the query box and label into the vbox */
    gtk_box_pack_start(GTK_BOX(vbox), label, FALSE, FALSE, SPACER);
    gtk_box_pack_start(GTK_BOX(vbox), query, FALSE, FALSE, SPACER);

    gtk_widget_show(query);
    gtk_widget_show(label);

    return vbox;
}
