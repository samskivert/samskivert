#
# $Id: lookuplet.py,v 1.1 2002/03/17 09:03:06 mdb Exp $
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

import gnome.applet
import gtk
import GDK
import gnome.ui
import gnome.uiconsts
import string
import re

import bindings
import keyval_util
import properties

class Lookuplet:
    "Handles the primary setup and operation of the lookuplet application."

    # in theory these should be defined by GDK but seem not to be
    SELECTION_PRIMARY = 1;
    CURRENT_TIME = 0;

    # a reference to our key bindings
    bindings = None;

    # our about box
    about = None;

    # a reference to the properties manager
    props = None;

    # whether or not we're running as a Gnome applet
    appletMode = 0;

    def __init__ (self, xmlui, bindings, props, appletMode):
        window = xmlui.get_widget("lookuplet");
        query = xmlui.get_widget("query");
        self.about = xmlui.get_widget("about");
        self.string_atom = None;
        self.bindings = bindings;
        self.props = props;
        self.appletMode = appletMode;

        # wire up our handlers
        nameFuncMap = {};
        for key in dir(self.__class__):
            nameFuncMap[key] = getattr(self, key);
        xmlui.signal_autoconnect(nameFuncMap);

        # if we're in applet mode, extract the UI and stick it into an
        # applet widget
        if (appletMode):
            applet = gnome.applet.AppletWidget("lookuplet");
            mainbox = xmlui.get_widget("mainbox");
            # lose the border
            mainbox.set_border_width(0);
            # and hide the prefs button
            prefs = xmlui.get_widget("prefs");
            prefs.hide();
            # remove everything from the window
            window.remove(mainbox);
            # and add it to the applet
            applet.add(mainbox);
            # register our menu items
            applet.register_stock_callback(
                "properties", gnome.uiconsts.STOCK_MENU_PROP,
                "Properties...", self.on_props_selected, None);
            applet.register_stock_callback(
                "about", gnome.uiconsts.STOCK_MENU_ABOUT,
                "About...", self.on_about_selected, None);
            applet.show();

        else:
            window.show();

        # request the selection
        if (self.string_atom == None):
            self.string_atom = gtk.atom_intern("STRING", gtk.FALSE);
        query.selection_convert(self.SELECTION_PRIMARY, 
                                self.string_atom,
                                self.CURRENT_TIME);

        # put the focus in the query box
        query.grab_focus();

    def on_props_selected (self, one, two):
        self.props.editProperties();

    def on_about_selected (self, one, two):
        self.about.show();

    def on_prefs_clicked (self, button):
        self.props.editProperties();

    def on_query_key_press_event (self, query, event):
        # handle special keystrokes such as history and auto-complete
        # if (handle_special_keys(widget, ek)):
        # return true;

        # ignore plain or shifted keystrokes
        if (event.state <= 1):
            return gtk.FALSE;

        # convert the key press into a string
        keystr = keyval_util.convert_keyval_state_to_string(
            event.keyval, event.state);

        # look up a binding for that key string
        binding = self.bindings.get_match(keystr);
        if (binding != None):
            # if we found one, invoke it
            binding.invoke(query.get_text());
            if (self.appletMode):
                query.set_text("");
            else:
                gtk.mainquit();
            return gtk.TRUE;

        else:
            # otherwise, let GTK know that we didn't handle the key press
            return gtk.FALSE;

    def on_query_selection_received (self, query, selection_data, data):
        if (selection_data.length < 0):
            # print "Selection retrieval failed.";
            return;

        if (selection_data.type != GDK.SELECTION_TYPE_STRING):
            print "Selection target not returned as a string.";
            return;

        print "Got selection '%s'." % selection_data.data;

        # prune spaces from the end of the text
        text = string.strip(selection_data.data);
        # compress whitespace (and convert newlines to spaces)
        text = re.sub("[ \r\n]+", " ", text);

        # make sure some selection was provided
        if (len(text) > 0):
            # check to see if we're looking at a url here
            # is_url = url_p(text);

            # if this is a URL, we want to eat whitespace
            # if (is_url):
            # text.tr!(" ", "");

            if (cmp(text, selection_data.data)):
                # print "Setting trimmed text '" + text  + "'.";
                query.set_text(text);

            query.select_region(0, len(text));
            # print "Selected from %d to %d." % (0, len(text));

    def exit_lookuplet (self, button):
        gtk.mainquit();
