/**
 * $Id: preferences.c,v 1.2 2001/08/16 20:25:09 mdb Exp $
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
#include <gdk/gdkx.h>

#include "keysym-util.h"
#include "binding.h"
#include "preferences.h"

static GtkWidget* _grabDialog;
static GtkWidget* _bindList;
static GtkWidget* _prefBox = NULL;
static GPtrArray* _bindings;

static GtkWidget* _keyEntry;
static LkBindingType _type;
static GtkWidget* _nameEntry;
static GtkWidget* _argEntry;
static GtkWidget* _edit;

static gint _selection = -1;

static GdkFilterReturn
grab_key_filter (GdkXEvent* gdk_xevent, GdkEvent* event, gpointer data)
{
    XEvent* xevent = (XEvent*)gdk_xevent;
    GtkEntry* entry;
    char* key;

    /* skip non-keypress events */
    if (xevent->type != KeyPress && xevent->type != KeyRelease) {
	return GDK_FILTER_CONTINUE;
    }	

    /* convert the keysym into a string and stick it into the text entry
     * that the user would otherwise type into */
    key = convert_keysym_state_to_string(event->key.keyval, event->key.state);
    entry = GTK_ENTRY(data);
    gtk_entry_set_text(entry, key ? key : "");
    g_free(key);

    /* clean up after ourselves */
    gdk_keyboard_ungrab(GDK_CURRENT_TIME);
    gtk_widget_destroy(_grabDialog);
    _grabDialog = NULL;
    gdk_window_remove_filter(GDK_ROOT_PARENT(), grab_key_filter, data);

    return GDK_FILTER_REMOVE;
}

static void
grab_button_pressed (GtkButton* button, gpointer data)
{
    GtkWidget* frame;
    GtkWidget* box;
    GtkWidget* label;

    _grabDialog = gtk_window_new(GTK_WINDOW_POPUP);

    gdk_keyboard_grab(GDK_ROOT_PARENT(), TRUE, GDK_CURRENT_TIME);
    gdk_window_add_filter(GDK_ROOT_PARENT(), grab_key_filter, data);

    gtk_window_set_policy(GTK_WINDOW(_grabDialog), FALSE, FALSE, TRUE);
    gtk_window_set_position(GTK_WINDOW(_grabDialog), GTK_WIN_POS_CENTER);
    gtk_window_set_modal(GTK_WINDOW(_grabDialog), TRUE);

    frame = gtk_frame_new(NULL);
    gtk_container_add(GTK_CONTAINER(_grabDialog), frame);

    box = gtk_hbox_new(FALSE, 0);
    gtk_container_set_border_width(GTK_CONTAINER(box), GNOME_PAD_BIG);
    gtk_container_add(GTK_CONTAINER(frame), box);

    label = gtk_label_new(_("Press a key..."));
    gtk_container_add(GTK_CONTAINER(box), label);

    gtk_widget_show_all(_grabDialog);
}

static void
pop_down_edit_binding (GtkWidget* button, gpointer arg)
{
    _keyEntry = NULL;
    _nameEntry = NULL;
    _argEntry = NULL;
    gtk_widget_destroy(GTK_WIDGET(arg));
}

static void
select_type (GtkWidget* menuitem, gpointer data)
{
    _type = (LkBindingType)GPOINTER_TO_INT(data);
}

static void
row_selected (GtkCList* clist, gint row, gint column, GdkEventButton* event,
	      gpointer user_data)
{
    _selection = row;
    gtk_widget_set_sensitive(_edit, TRUE);
}

static void
row_deselected (GtkCList* clist, gint row, gint column, GdkEventButton* event,
	      gpointer user_data)
{
    _selection = -1;
    gtk_widget_set_sensitive(_edit, FALSE);
}

static void
populate_binding (GtkWidget* button, gpointer arg)
{
    LkBinding* binding = LK_BINDING(arg);
    gchar* key = gtk_entry_get_text(GTK_ENTRY(_keyEntry));
    gchar* name = gtk_entry_get_text(GTK_ENTRY(_nameEntry));
    gchar* argument = gtk_entry_get_text(GTK_ENTRY(_argEntry));

    /* only update the binding if we modified something */
    if (strcmp(key, binding->key) || _type != binding->type ||
	strcmp(name, binding->name) || strcmp(argument, binding->argument)) {
	/* update the binding instance */
	lk_binding_init(binding, key, _type, name, argument);
	/* let the prefs system know that something has changed */
	gnome_property_box_changed(GNOME_PROPERTY_BOX(_prefBox));
    }
}

static void
lk_prefs_edit_binding (LkBinding* binding, GtkSignalFunc okedFunc,
		       GtkSignalFunc cancelledFunc)
{
    GtkWidget* dialog;
    GtkWidget* vbox, *hbox;
    GtkWidget* label, *button;
    GtkWidget* optmenu, *menu, *menuitem;

    gchar* types[] = { _("URL"), _("Exec"), NULL };
    int i;

    dialog = gtk_dialog_new();
    gtk_window_set_modal(GTK_WINDOW(dialog), TRUE);
    vbox = GTK_DIALOG(dialog)->vbox;
    gtk_container_set_border_width(GTK_CONTAINER(vbox),
				   GNOME_PAD_SMALL);
    gtk_box_set_spacing(GTK_BOX(vbox), GNOME_PAD_SMALL);

    /* create the key line */
    hbox = gtk_hbox_new(FALSE, GNOME_PAD_SMALL);
    /* create the key input */
    label = gtk_label_new(_("Key:"));
    gtk_box_pack_start(GTK_BOX(hbox), label, FALSE, FALSE, 0);
    _keyEntry = gtk_entry_new();
    gtk_entry_set_text(GTK_ENTRY(_keyEntry), binding->key);
    gtk_box_pack_start(GTK_BOX(hbox), _keyEntry, FALSE, FALSE, 0);
    button = gtk_button_new_with_label(_("Grab..."));
    gtk_signal_connect(GTK_OBJECT(button), "clicked",
		       grab_button_pressed, _keyEntry);
    gtk_box_pack_start(GTK_BOX(hbox), button, FALSE, FALSE, 0);
    /* create the name input */
    label = gtk_label_new(_("Name:"));
    gtk_box_pack_start(GTK_BOX(hbox), label, FALSE, FALSE, 0);
    _nameEntry = gtk_entry_new();
    gtk_entry_set_text(GTK_ENTRY(_nameEntry), binding->name);
    gtk_box_pack_start(GTK_BOX(hbox), _nameEntry, FALSE, FALSE, 0);
    /* pack the line into the dialog */
    gtk_box_pack_start(GTK_BOX(vbox), hbox, FALSE, FALSE, 0);

    /* create the command line */
    hbox = gtk_hbox_new(FALSE, GNOME_PAD_SMALL);
    optmenu = gtk_option_menu_new();
    menu = gtk_menu_new();
    for (i = 0; types[i] != NULL; i++) {
	menuitem = gtk_menu_item_new_with_label(types[i]);
	gtk_signal_connect(GTK_OBJECT(menuitem), "activate",
			   select_type, GINT_TO_POINTER(i));
	gtk_widget_show(menuitem);
	gtk_menu_append(GTK_MENU(menu), menuitem);
    }
    gtk_option_menu_set_menu(GTK_OPTION_MENU(optmenu), menu);
    gtk_option_menu_set_history(GTK_OPTION_MENU(optmenu),
				(int)binding->type);
    _type = binding->type;
    gtk_box_pack_start(GTK_BOX(hbox), optmenu, FALSE, FALSE, 0);

    _argEntry = gtk_entry_new_with_max_length(MAX_BINDING_LENGTH);
    gtk_entry_set_text(GTK_ENTRY(_argEntry), binding->argument);
    gtk_box_pack_start(GTK_BOX(hbox), _argEntry, TRUE, TRUE, 0);
    gtk_box_pack_start(GTK_BOX(vbox), hbox, FALSE, FALSE, 0);

    /* create and wire up the cancel button */
    hbox = GTK_DIALOG(dialog)->action_area;
    button = gtk_button_new_with_label(_("Cancel"));
    gtk_box_pack_end(GTK_BOX(hbox), button, FALSE, FALSE, 0);
    if (cancelledFunc != NULL) {
	gtk_signal_connect(GTK_OBJECT(button), "clicked",
			   cancelledFunc, binding);
    }
    gtk_signal_connect(GTK_OBJECT(button), "clicked",
		       pop_down_edit_binding, dialog);

    /* create and wire up the ok button */
    button = gtk_button_new_with_label(_("OK"));
    gtk_signal_connect(GTK_OBJECT(button), "clicked",
		       populate_binding, binding);
    if (okedFunc != NULL) {
	gtk_signal_connect(GTK_OBJECT(button), "clicked",
			   okedFunc, binding);
    }
    gtk_signal_connect(GTK_OBJECT(button), "clicked",
		       pop_down_edit_binding, dialog);
    gtk_box_pack_end(GTK_BOX(hbox), button, FALSE, FALSE, 0);

    gtk_window_set_default_size(GTK_WINDOW(dialog), 400, 10);
    gtk_widget_show_all(dialog);
}

static void
add_binding_oked (GtkWidget* button, gpointer arg)
{
    LkBinding* binding = LK_BINDING(arg);
    gchar* data[2];

    /* add this binding to our bindings array */
    g_ptr_array_add(_bindings, binding);

    /* and add it to the bindings list */
    data[0] = binding->key;
    data[1] = binding->name;
    gtk_clist_append(GTK_CLIST(_bindList), data);
    gtk_clist_columns_autosize(GTK_CLIST(_bindList));

    /* indicate our change of properties */
    gnome_property_box_changed(GNOME_PROPERTY_BOX(_prefBox));
}

static void
add_binding_cancelled (GtkWidget* button, gpointer arg)
{
    lk_binding_destroy(LK_BINDING(arg));
}

static void
add_clicked (GtkButton* button, gpointer user_data)
{
    LkBinding* binding = lk_binding_new();
    lk_prefs_edit_binding(binding, add_binding_oked, add_binding_cancelled);
}

static void
edit_binding_oked (GtkWidget* button, gpointer arg)
{
    LkBinding* binding = LK_BINDING(arg);

    /* update the table display (the _selection is guaranteed not to have
     * changed because the edit dialog is modal) */
    gtk_clist_set_text(GTK_CLIST(_bindList), _selection, 0, binding->key);
    gtk_clist_set_text(GTK_CLIST(_bindList), _selection, 1, binding->name);
    gtk_clist_columns_autosize(GTK_CLIST(_bindList));
}

static void
edit_clicked (GtkButton* button, gpointer user_data)
{
    /* only do something if there's a selection */
    if (_selection != -1) {
	LkBinding* binding =
	    LK_BINDING(g_ptr_array_index(_bindings, _selection));
	/* make sure things aren't crazily out of sync */
	if (binding != NULL) {
	    lk_prefs_edit_binding(binding, edit_binding_oked, NULL);
	}
    }
}

static void
delete_clicked (GtkButton* button, gpointer user_data)
{
    /* only do something if there's a selection */
    if (_selection != -1) {
	/* remove the binding from our bindings list */
	LkBinding* binding =
	    LK_BINDING(g_ptr_array_remove_index(_bindings, _selection));
	/* make sure things are crazily out of sync */
	if (binding != NULL) {
	    lk_binding_destroy(binding);
	}
	/* and remove that row from the clist */
	gtk_clist_remove(GTK_CLIST(_bindList), _selection);
	/* clear out the selection */
	_selection = -1;
	/* let the world know we've made changes */
	gnome_property_box_changed(GNOME_PROPERTY_BOX(_prefBox));
    }
}

void
lk_prefs_init (void)
{
    if (_bindings == NULL) {
	_bindings = g_ptr_array_new();

	/* load up our bindings */
	lk_binding_load_bindings(_bindings);
    }
}

GPtrArray*
lk_prefs_get_bindings (void)
{
    return _bindings;
}

void
lk_prefs_cleanup (void)
{
    int i;

    /* free our preferences panel */
    gtk_widget_destroy(_prefBox);
    _prefBox = NULL;

    for (i = 0; i < _bindings->len; i++) {
	lk_binding_destroy(LK_BINDING(g_ptr_array_index(_bindings, i)));
    }
    g_ptr_array_free(_bindings, FALSE);
}

static void
populate_bindings ()
{
    gchar* data[2];
    int i;

    /* clear out any old stuff */
    gtk_clist_clear(GTK_CLIST(_bindList));

    /* add an entry for each binding */
    for (i = 0; i < _bindings->len; i++) {
	LkBinding* binding = LK_BINDING(g_ptr_array_index(_bindings, i));
	data[0] = binding->key;
	data[1] = binding->name;
	gtk_clist_append(GTK_CLIST(_bindList), data);
    }

    /* fit the display to the contents */
    gtk_clist_columns_autosize(GTK_CLIST(_bindList));
}

static void
apply_preferences (GnomePropertyBox* pbox, gint page, gpointer data)
{
    /* save our bindings */
    lk_binding_save_bindings(_bindings);
}

void
lk_prefs_display ()
{
    GtkWidget* vbox, *bbox, *scrolled;
    GtkWidget* label;
    GtkWidget* add, *delete;

    gchar* titles[] = { _("Key"), _("Name") };

    /* just display the existing window if it's already open */
    if (_prefBox != NULL) {
	populate_bindings();
        gdk_window_show(GTK_WIDGET(_prefBox)->window);
	gdk_window_raise(GTK_WIDGET(_prefBox)->window);
	return;
    }

    /* create and configure our prefs ui */
    _prefBox = gnome_property_box_new();
    gtk_window_set_title(GTK_WINDOW(_prefBox), _("Lookuplet Properties"));
    gtk_signal_connect(GTK_OBJECT(_prefBox), "destroy",
		       gtk_widget_destroyed, &_prefBox);;

    vbox = gtk_vbox_new(FALSE, GNOME_PAD_SMALL);
    gtk_container_set_border_width(GTK_CONTAINER(vbox),
				   GNOME_PAD_SMALL);

    label = gtk_label_new(_("Configure bindings..."));
    gtk_label_set_justify(GTK_LABEL(label), GTK_JUSTIFY_LEFT);
    gtk_box_pack_start(GTK_BOX(vbox), label, FALSE, FALSE, 0);

    /* create a scrolled window in which our clist will live */
    scrolled = gtk_scrolled_window_new(NULL, NULL);
    gtk_scrolled_window_set_policy(GTK_SCROLLED_WINDOW(scrolled),
				   GTK_POLICY_AUTOMATIC,
				   GTK_POLICY_AUTOMATIC);
    _bindList = gtk_clist_new_with_titles(2, titles);
    gtk_clist_set_selection_mode(GTK_CLIST(_bindList), GTK_SELECTION_SINGLE);
    gtk_signal_connect(GTK_OBJECT(_bindList), "select-row",
		       GTK_SIGNAL_FUNC(row_selected), NULL);
    gtk_signal_connect(GTK_OBJECT(_bindList), "unselect-row",
		       GTK_SIGNAL_FUNC(row_deselected), NULL);
    gtk_container_add(GTK_CONTAINER(scrolled), _bindList);

    /* instruct the scrolled window not to assume teeny dimensions */
    gtk_widget_set_usize(scrolled, 300, 200);

    populate_bindings();
    gtk_box_pack_start(GTK_BOX(vbox), scrolled, TRUE, TRUE, 0);

    /* create the control buttons */
    bbox = gtk_hbox_new(FALSE, GNOME_PAD_SMALL);
    delete = gtk_button_new_with_label(_("Delete"));
    gtk_box_pack_end(GTK_BOX(bbox), delete, FALSE, FALSE, 0);
    _edit = gtk_button_new_with_label(_("Edit..."));
    gtk_widget_set_sensitive(_edit, FALSE);
    gtk_box_pack_end(GTK_BOX(bbox), _edit, FALSE, FALSE, 0);
    add = gtk_button_new_with_label(_("Add..."));
    gtk_box_pack_end(GTK_BOX(bbox), add, FALSE, FALSE, 0);
    gtk_box_pack_start(GTK_BOX(vbox), bbox, FALSE, FALSE, 0);

    /* wire them up */
    gtk_signal_connect(GTK_OBJECT(add), "clicked",
		       GTK_SIGNAL_FUNC(add_clicked), NULL);
    gtk_signal_connect(GTK_OBJECT(_edit), "clicked",
		       GTK_SIGNAL_FUNC(edit_clicked), NULL);
    gtk_signal_connect(GTK_OBJECT(delete), "clicked",
		       GTK_SIGNAL_FUNC(delete_clicked), NULL);

    /* add our page to the property box */
    gnome_property_box_append_page(GNOME_PROPERTY_BOX(_prefBox),
				   vbox, gtk_label_new(_("Bindings")));

    /* wire up some signals */
    gtk_signal_connect(GTK_OBJECT(_prefBox), "apply",
		       GTK_SIGNAL_FUNC(apply_preferences), NULL);

    /* and finally show our property box */
    gtk_widget_show_all(_prefBox);
}
