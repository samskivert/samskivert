#
# $Id: properties.py,v 1.3 2003/11/28 21:34:59 mdb Exp $
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

import string
import re

import gobject
import gtk
import gtk.gdk
import gnome.ui

import edit_binding

class Properties:
    # our configured key bindings
    bindings = None;

    # the properties panel
    propsPanel = None;

    # our binding editor
    bindEditor = None;

    # the bindings model
    bindModel = None;

    # the edit button
    editButton = None;

    # the delete button
    deleteButton = None;

    # the selected binding index
    selection = None;

    def __init__ (self, xmlui, bindings):
        # keep a handle on our bindings
        self.bindings = bindings;

        # create our binding editor
        self.bindEditor = edit_binding.BindingEditor(xmlui, self);

        # get a reference to some widgets
        self.propsPanel = xmlui.get_widget("properties");
        self.editButton = xmlui.get_widget("edit");
        self.deleteButton = xmlui.get_widget("delete");

        # make our props panel not destroy itself on close
        self.propsPanel.close_hides(gtk.TRUE);

        # wire up our handlers
        xmlui.signal_connect("on_properties_apply", self.on_properties_apply);
        xmlui.signal_connect("on_add_clicked", self.on_add_clicked);
        xmlui.signal_connect("on_edit_clicked", self.on_edit_clicked);
        xmlui.signal_connect("on_delete_clicked", self.on_delete_clicked);

        # set up our tree view
        bindList = xmlui.get_widget("bindings");
        renderer = gtk.CellRendererText();
        bindList.append_column(gtk.TreeViewColumn("Key", renderer, text=0));
        bindList.append_column(gtk.TreeViewColumn("Name", renderer, text=1));

        # create a data model for our bindings list view
        self.bindModel = gtk.ListStore(gobject.TYPE_STRING,
                                       gobject.TYPE_STRING,
                                       gobject.TYPE_PYOBJECT);

        # configure our properties panel with the loaded bindings
        for binding in bindings.bindings:
            iter = self.bindModel.append()
            self.bindModel.set_value(iter, 0, binding.key);
            self.bindModel.set_value(iter, 1, binding.name);
            self.bindModel.set_value(iter, 2, binding);

        bindList.set_model(self.bindModel);

        # wire up our selection monitor
        selection = bindList.get_selection();
        selection.connect("changed", self.on_bindings_selection_changed);

    def editProperties (self):
        self.propsPanel.show();
        return;

    def updated (self, iter, binding):
        # refresh the display for the specified index
        self.bindModel.set_value(iter, 0, binding.key);
        self.bindModel.set_value(iter, 1, binding.name);
        # and make a note that we've updated ourselves
        self.propsPanel.changed();

    def created (self, binding):
        # add the new binding to our list
        self.bindings.bindings.append(binding);
        # add it to the display
        iter = self.bindModel.append()
        self.bindModel.set_value(iter, 0, binding.key);
        self.bindModel.set_value(iter, 1, binding.name);
        self.bindModel.set_value(iter, 2, binding);
        # and make a note that we've updated ourselves
        self.propsPanel.changed();

    def on_bindings_selection_changed (self, selection):
        # make a note of the iterator for the selected binding
        model, self.selection = selection.get_selected();

        # no ?: notation? egads!
        if (self.selection == None):
            sensitive = gtk.FALSE;
        else:
            sensitive = gtk.TRUE;

        # enable our buttons
        self.editButton.set_sensitive(sensitive);
        self.deleteButton.set_sensitive(sensitive);

        # make like they pressed the edit button on a double click
        # if (event.type == gtk.gdk._2BUTTON_PRESS):
        #    self.on_edit_clicked(None);

    def on_properties_apply (self, panel, page):
        if (page == 0):
            self.bindings.flush();

    def on_add_clicked (self, button):
        # show the binding creation dialog
        self.bindEditor.createBinding();
        return;

    def on_edit_clicked (self, button):
        # tell the binding editor to edit this binding
        binding = self.bindModel.get_value(self.selection, 2);
        self.bindEditor.editBinding(self.selection, binding);
        return;

    def on_delete_clicked (self, button):
        # remove the binding from the display and our bindings list
        binding = self.bindModel.get_value(self.selection, 2);
        self.bindModel.remove(self.selection)
        self.bindings.bindings.remove(binding);
        self.selection = None;
        self.propsPanel.changed();
        return;
