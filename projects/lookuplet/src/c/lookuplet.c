/**
 * $Id: lookuplet.c,v 1.7 2002/03/14 16:45:26 shaper Exp $
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
#include <applet-widget.h>
#include <gnome.h>

#include "querybox.h"
#include "preferences.h"

/* Used to track whether or not we're being run as an applet or
 * standalone. */
int applet_mode = 0;

static void
exit_lookuplet (GtkWidget* widget, gpointer data)
{
    gtk_main_quit();
}

static void
about_box_cb (AppletWidget *applet, gpointer data)
{
    static GtkWidget* about_box = NULL;
    const gchar* authors[] = {
        (gchar*) "Michael Bayne <mdb@samskivert.com>", (gchar*)NULL };

    if (about_box != NULL) {
        gdk_window_show(about_box->window);
        gdk_window_raise(about_box->window);
        return;
    }

    about_box = gnome_about_new(
        _("lookuplet"), VERSION, "(C) 2001 Michael Bayne", authors,
        _("This GNOME applet provides a simple means by which web and "
          "other queries can be launched with minimal typing.\n\n\n"
          "This program is free software; you can redistribute it and/or "
          "modify it under the terms of the GNU General Public License as "
          "published by the Free Software Foundation; either version 2 of "
          "the License, or (at your option) any later version."),
        (gchar*)NULL);
    gtk_signal_connect(GTK_OBJECT(about_box), "destroy",
                       GTK_SIGNAL_FUNC(gtk_widget_destroyed), &about_box);
    gtk_widget_show(about_box);
}

int
main (int argc, char** argv)
{
    GtkWidget* top;
    GtkWidget* contents;
    int i;

    /* figure out if we're running as an applet */
    for (i = 0; i < argc; i++) {
        if (strstr(argv[i], "activate-goad-server")) {
            applet_mode = 1;
        }
    }

#ifdef ENABLE_NLS
    /* initialize the i18n stuff */
    bindtextdomain(PACKAGE, GNOMELOCALEDIR);
    textdomain(PACKAGE);
#endif

    if (applet_mode) {
        /* initialize; this will basically set up the applet, corba and
           call gnome_init() */
        applet_widget_init(PACKAGE, VERSION, argc, argv, NULL, 0, NULL);

        /* create our applet_widget */
        if (!(top = applet_widget_new(PACKAGE))) {
            g_error("Can't create applet widget.\n");
            exit(1);
        }

    } else {
        /* initialize gnome */
        gnome_init(PACKAGE, VERSION, argc, argv);

        /* create a new window */
        top = gtk_window_new(GTK_WINDOW_TOPLEVEL);
        gtk_signal_connect(GTK_OBJECT(top), "destroy",
                           GTK_SIGNAL_FUNC(exit_lookuplet), NULL);
    }

    /* initialize our preferences */
    lk_prefs_init();

    /* create our contents and set them up real proper like */
    contents = lk_querybox_create();

    if (applet_mode) {
        applet_widget_add(APPLET_WIDGET(top), contents);

    } else {
        /* we only want a border if we're not an applet */
        gtk_container_set_border_width(GTK_CONTAINER(top), GNOME_PAD_SMALL);
        gtk_container_add(GTK_CONTAINER(top), contents);
    }

    gtk_widget_show_all(contents);
    gtk_widget_show(top);

    /* set up our applet menu */
    if (applet_mode) {
        applet_widget_register_stock_callback(
            APPLET_WIDGET(top), "properties", GNOME_STOCK_MENU_PROP,
            _("Properties..."), lk_prefs_display_applet, NULL);
        applet_widget_register_stock_callback(
            APPLET_WIDGET(top), "about", GNOME_STOCK_MENU_ABOUT,
            _("About..."), about_box_cb, NULL);
    }

    /* initialize the querybox (which handles about everything) */
    lk_querybox_init();

    /* main loop */
    if (applet_mode) {
        applet_widget_gtk_main();
    } else {
        gtk_main();
    }

    return 0;
}
