// generated 2002/12/1 21:56:20 PST by mdb@baltic.(none)
// using glademm V1.1.3c_cvs
//
// newer (non customized) versions of this file go to binding.hh_new

// you might replace
//    class foo : public foo_glade { ... };
// by
//    typedef foo_glade foo;
// if you didn't make any modifications to the widget

#ifndef _BINDING_HH
#  include "binding_glade.hh"
#  define _BINDING_HH
class binding : public binding_glade
{  
        
        void on_ok_clicked();
        void on_cancel_clicked();
        bool on_key_key_press_event(GdkEventKey *ev);
};
#endif
