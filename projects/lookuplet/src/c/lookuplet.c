/**
 * $Id: lookuplet.c,v 1.5 2001/08/16 20:25:09 mdb Exp $
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

#ifdef ENABLE_NLS
    /* initialize the i18n stuff */
    bindtextdomain(PACKAGE, GNOMELOCALEDIR);
    textdomain(PACKAGE);
#endif

#ifdef APPLET_MODE
    /* initialize gtk */
    gtk_init(&argc, &argv);
#else
    /* initialize gnome */
    gnome_init(PACKAGE, VERSION, argc, argv);
#endif

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
