//
// $Id: lookuplet.cc,v 1.2 2003/11/30 23:50:18 mdb Exp $

#include <cstdlib>
#include <iostream>

#include <config.h>
#include <gtk/gtk.h>
#include <libgnomemm/main.h>
#include <libgnomeuimm/init.h>
// #include <bonobomm/widgets/wrap_init.h>

#include "lookuplet.hh"
#include "binding.hh"
#include "about.hh"
#include "properties.hh"

using namespace std;

static void got_clip_text (
    GtkClipboard* clipboard, const gchar* text, gpointer data)
{
    ((lookuplet*)data)->on_clip_text_received(text);
}

bool lookuplet::on_query_key_press_event (GdkEventKey* ev)
{
    printf("key pressed %d %d\n", ev->type, ev->keyval);
    return 0;
}

void lookuplet::on_clip_text_received (const gchar* text)
{
    _query->set_text(text);
}

void lookuplet::on_prefs_clicked ()
{
    manage(new class properties());
}

void lookuplet::exit_lookuplet ()
{
}

bool Gtk::Widget::on_delete_event (GdkEventAny* event)
{
    exit(0);
}

int main (int argc, char** argv)
{
    Gnome::Main m(PACKAGE, VERSION, Gnome::UI::module_info_get(), argc, argv);
//    Gnome::Bonobo::wrap_init();

    lookuplet* lookuplet = new class lookuplet();
//     binding *binding = new class binding();
//     about *about = new class about();
//     properties *properties = new class properties();

    GtkClipboard* clip = gtk_clipboard_get(GDK_SELECTION_PRIMARY);
    gtk_clipboard_request_text(clip, &got_clip_text, lookuplet);
    m.run(*lookuplet);

    delete lookuplet;
//     delete binding;
//     delete about;
//     delete properties;

    return 0;
}
