#
# $Id: properties.py,v 1.2 2002/03/18 00:30:24 mdb Exp $
# 
# lookuplet - a utility for quickly looking up information
# Copyright (C) 2001-2002 Michael Bayne
# 
# This program is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; either version 2.1 of the License, or (at your
# option) any later version.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

import gtk
import GDK
import gnome.ui
import libglade
import string
import re

import edit_binding

class Properties:
    # our configured key bindings
    bindings = None;

    # the properties panel
    propsPanel = None;

    # our binding editor
    bindEditor = None;

    # the bindings list
    bindList = None;

    # the edit button
    editButton = None;

    # the delete button
    deleteButton = None;

    # the selected binding index
    selection = -1;

    def __init__ (self, xmlui, bindings):
        # keep a handle on our bindings
        self.bindings = bindings;

        # create our binding editor
        self.bindEditor = edit_binding.BindingEditor(xmlui, self, bindings);

        # get a reference to some widgets
        self.propsPanel = xmlui.get_widget("properties");
        self.editButton = xmlui.get_widget("edit");
        self.deleteButton = xmlui.get_widget("delete");
        self.bindList = xmlui.get_widget("bindings");

        # make our props panel not destroy itself on close
        self.propsPanel.close_hides(gtk.TRUE);

        # wire up our handlers
        nameFuncMap = {};
        for key in dir(self.__class__):
            nameFuncMap[key] = getattr(self, key);
        xmlui.signal_autoconnect(nameFuncMap);

        # configure our properties panel with the loaded bindings
        for binding in bindings.bindings:
            self.bindList.append([binding.key, binding.name]);

    def editProperties (self):
        self.propsPanel.show();
        return;

    def updated (self, index):
        binding = self.bindings.bindings[index];
        # refresh the display for the specified index
        self.bindList.set_text(index, 0, binding.key);
        self.bindList.set_text(index, 1, binding.name);
        # and make a note that we've updated ourselves
        self.propsPanel.changed();

    def created (self, binding):
        # add the new binding to our list
        self.bindings.bindings.append(binding);
        # add it to the display
        self.bindList.append([binding.key, binding.name]);
        # and make a note that we've updated ourselves
        self.propsPanel.changed();

    def on_properties_apply (self, panel, page):
        if (page == 0):
            self.bindings.flush();

    def on_bindings_select_row (self, clist, row, column, event):
        # enable our buttons
        self.editButton.set_sensitive(gtk.TRUE);
        self.deleteButton.set_sensitive(gtk.TRUE);

        # make a note of the selected binding index
        self.selection = row;

        # make like they pressed the edit button on a double click
        if (event.type == GDK._2BUTTON_PRESS):
            self.on_edit_clicked(None);

    def on_bindings_unselect_row (self, clist, row, column, event):
        # disable our buttons
        self.editButton.set_sensitive(gtk.FALSE);
        self.deleteButton.set_sensitive(gtk.FALSE);

    def on_add_clicked (self, button):
        # show the binding creation dialog
        self.bindEditor.createBinding();
        return;

    def on_edit_clicked (self, button):
        # tell the binding editor to edit this binding
        self.bindEditor.editBinding(self.selection);
        return;

    def on_delete_clicked (self, button):
        # remove the binding from the display and our bindings list
        binding = self.bindings.bindings[self.selection];
        self.bindList.remove(self.selection)
        self.bindings.bindings.remove(binding);
        self.selection = -1;
        self.propsPanel.changed();
        return;
