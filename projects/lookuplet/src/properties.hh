//
// $Id: properties.hh,v 1.1 2003/11/28 21:34:59 mdb Exp $

#ifndef _PROPERTIES_HH
#include "properties_glade.hh"
#define _PROPERTIES_HH

class properties : public properties_glade
{  
    friend class properties_glade;

    void on_delete_clicked ();
    void on_edit_clicked ();
    void on_add_clicked ();
    void on_close_clicked ();
};

#endif
