#
# $Id: edit_binding.py,v 1.2 2003/11/28 21:34:59 mdb Exp $
# 
# lookuplet - a utility for quickly looking up information
# Copyright (C) 2001 Michael Bayne
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

import gtk
import gnome.ui

import bindings
import keyval_util

class BindingEditor:
    # the properties instance for which we're editing bindings
    props = None;

    # our bindings set
    bindings = None;

    # the binding editor panel
    bindPanel = None;

    # the grab window
    grabWindow = None;

    # the iterator for the binding we're editing
    iterator = None;

    # the binding we're editing
    binding = None;

    # the key entry field
    keyField = None;

    # the name entry field
    nameField = None;

    # the argument type selector
    argTypeSel = None;

    # the URL menu item of the arg selection widget
    urlItem = None;

    # the argument entry field
    argField = None;

    def __init__ (self, xmlui, props):
        # keep these around for later
        self.props = props;

        # get a reference to some widgets
        self.bindPanel = xmlui.get_widget("binding");
        self.grabWindow = xmlui.get_widget("grab");
        self.keyField = xmlui.get_widget("key");
        self.nameField = xmlui.get_widget("name");
        self.argTypeSel = xmlui.get_widget("type");
        self.urlItem = self.argTypeSel.get_menu().get_active();
        self.argField = xmlui.get_widget("argument");

        # wire up our handlers
        xmlui.signal_connect("on_ok_clicked", self.on_ok_clicked);
        xmlui.signal_connect("on_cancel_clicked", self.on_cancel_clicked);
        xmlui.signal_connect("on_key_key_press_event",
                             self.on_key_key_press_event);

    #
    # Instructs the binding panel to display itself, configured for
    # editing the supplied binding.
    #
    def editBinding (self, iterator, binding):
        self.iterator = iterator;
        self.binding = binding;
        self.populateAndShow(self.binding);

    #
    # Instructs the binding panel to create a blank binding and make that
    # available for editing by the user.
    #
    def createBinding (self):
        self.iterator = None;
        # create a blank binding and edit it
        self.binding = bindings.Binding("", bindings.Binding.URL, "", "");
        self.populateAndShow(self.binding);

    #
    # A helper function used to populate our widgets and show the dialog.
    #
    def populateAndShow (self, binding):
        # populate our widgets with the binding values
        self.keyField.set_text(binding.key);
        self.nameField.set_text(binding.name);
        self.argTypeSel.set_history(binding.type);
        self.argField.set_text(binding.argument);

        # and show the bind dialog
        self.bindPanel.show();

    #
    # A callback, called when the OK button is clicked.
    #
    def on_ok_clicked (self, button):
        # figure out which type menu item is selected; what a hack
        active = self.argTypeSel.get_menu().get_active();
        if (active == self.urlItem):
            type = bindings.Binding.URL;
        else:
            type = bindings.Binding.EXEC;

        # repopulate our binding
        modified = self.binding.update(self.keyField.get_text(), type,
                                       self.nameField.get_text(),
                                       self.argField.get_text());

        # let the props panel know if there were any modifications
        if (modified):
            if (self.iterator == None):
                self.props.created(self.binding);
            else:
                self.props.updated(self.iterator, self.binding);

        # hide the binding panel
        self.bindPanel.hide();

    #
    # A callback, called when the cancel button is clicked.
    #
    def on_cancel_clicked (self, button):
        # hide the binding panel
        self.bindPanel.hide();

    def on_key_key_press_event (self, textfield, event):
        # ignore plain or shifted keystrokes
        if (event.state > 1):
            # convert the key press into a string
            keystr = keyval_util.convert_keyval_state_to_string(
                event.keyval, event.state);
            textfield.set_text(keystr);
            return gtk.TRUE;
        return gtk.FALSE;
