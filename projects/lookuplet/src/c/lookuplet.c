/**
 * $Id: lookuplet.c,v 1.3 2001/02/24 02:45:18 mdb Exp $
 */
 
#include <config.h>
#include <gnome.h>

#include "querybox.h"
#include "preferences.h"

static void
exit_lookuplet (GtkWidget* widget, gpointer data)
{
    gtk_main_quit();
}

int
main (int argc, char** argv)
{
    GtkWidget* window;
    GtkWidget* contents;

    /* initialize the i18n stuff */
    bindtextdomain(PACKAGE, GNOMELOCALEDIR);
    textdomain(PACKAGE);

    /* initialize gtk */
    gtk_init(&argc, &argv);

    /* initialize our preferences */
    lk_prefs_init();

    /* create a new window */
    window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    gtk_signal_connect(GTK_OBJECT(window), "destroy",
		       GTK_SIGNAL_FUNC(exit_lookuplet), NULL);

    /* create our contents and set them up real proper like */
    contents = lk_querybox_create();
    gtk_container_add(GTK_CONTAINER(window), contents);
    gtk_container_set_border_width(GTK_CONTAINER(window), GNOME_PAD_SMALL);
    gtk_widget_show_all(contents);
    gtk_widget_show(window);

    /* initialize the querybox (which handles about everything) */
    lk_querybox_init();

    /* main loop */
    gtk_main();

    return 0;
}
