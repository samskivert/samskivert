//
// $Id: lookuplet.cc,v 1.1 2003/11/28 21:34:59 mdb Exp $

#include <cstdlib>
#include <iostream>

#include <config.h>
#include <libgnomemm/main.h>
#include <libgnomeuimm/init.h>
// #include <bonobomm/widgets/wrap_init.h>

#include "lookuplet.hh"
#include "binding.hh"
#include "about.hh"
#include "properties.hh"

using namespace std;

bool lookuplet::on_query_key_press_event (GdkEventKey *ev)
{
    return 0;
}

void lookuplet::on_query_selection_received (GtkSelectionData *data, guint time)
{
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

int main(int argc, char **argv)
{
    Gnome::Main m(PACKAGE, VERSION, Gnome::UI::module_info_get(), argc, argv);
//    Gnome::Bonobo::wrap_init();

    lookuplet *lookuplet = new class lookuplet();
//     binding *binding = new class binding();
//     about *about = new class about();
//     properties *properties = new class properties();

    m.run(*lookuplet);

    delete lookuplet;
//     delete binding;
//     delete about;
//     delete properties;

    return 0;
}
