/**
 * $Id: lookuplet.c,v 1.1 2000/12/10 23:38:39 mdb Exp $
 */
 
#include <config.h>
#include <gnome.h>
#include <applet-widget.h>

#include "querybox.h"

int
main (int argc, char** argv)
{
    GtkWidget* applet;
    GtkWidget* contents;

    /* initialize the i18n stuff */
    bindtextdomain(PACKAGE, GNOMELOCALEDIR);
    textdomain(PACKAGE);

    /* intialize, this will basically set up the applet, corba and
       call gnome_init */
    applet_widget_init("lookuplet", NULL, argc, argv, NULL, 0, NULL);

    /* create a new applet_widget */
    applet = applet_widget_new("lookuplet");
    if (applet == NULL) {
	g_error("Can't create applet!\n");
    }

    /* create our contents and set them up real proper like */
    contents = lookuplet_querybox_create();
    applet_widget_add(APPLET_WIDGET(applet), contents);
    gtk_widget_show(contents);
    gtk_widget_show(applet);

    /* special corba main loop */
    applet_widget_gtk_main();

    return 0;
}
