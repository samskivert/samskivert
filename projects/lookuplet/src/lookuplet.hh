//
// $Id: lookuplet.hh,v 1.1 2003/11/28 21:34:59 mdb Exp $

#ifndef _LOOKUPLET_HH
#include "lookuplet_glade.hh"
#define _LOOKUPLET_HH

class lookuplet : public lookuplet_glade
{  
    friend class lookuplet_glade;
    bool on_query_key_press_event(GdkEventKey *ev);
    void on_query_selection_received(GtkSelectionData *data, guint time);
    void on_prefs_clicked();
    void exit_lookuplet();
};

#endif
