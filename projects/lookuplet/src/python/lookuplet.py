#
# $Id: lookuplet.py,v 1.3 2002/03/17 21:25:20 mdb Exp $
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
import history
import keyval_util
import properties

class Lookuplet:
    "Handles the primary setup and operation of the lookuplet application."

    # in theory these should be defined by GDK but seem not to be
    SELECTION_PRIMARY = 1;
    CURRENT_TIME = 0;

    # our special key definitions
    PREV_HISTORY_KEY = GDK.Up
    NEXT_HISTORY_KEY = GDK.Down
    AUTO_COMPLETE_KEY = GDK.Tab

    # a reference to our key bindings
    bindings = None;

    # our about box
    about = None;

    # a reference to the properties manager
    props = None;

    # whether or not we're running as a Gnome applet
    appletMode = 0;

    # our query box history
    history = None;

    # where we are if we're scanning the history
    hisidx = -1;

    # the prefix we're using to match if we're matching on a prefix
    hispref = None;

    def __init__ (self, xmlui, bindings, props, appletMode):
        window = xmlui.get_widget("lookuplet");
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

        # create our query history
        self.history = history.History();

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
        query = xmlui.get_widget("query");
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

    def handle_special_keys (self, query, event):
        # we'll need this later
        count = len(self.history.history);

        # handle special keystrokes such as history and auto-complete
        if (event.keyval == self.NEXT_HISTORY_KEY):
            if (self.hisidx >= 0 and self.hisidx < count):
                self.hisidx += 1;
            else:
                return gtk.TRUE;

        elif (event.keyval == self.PREV_HISTORY_KEY):
            if (self.hisidx > 0):
                self.hisidx -= 1;
            elif (self.hisidx == -1):
                # if we haven't grabbed a prefix as of yet, grab what we've got
                if (self.hispref == None):
                    self.hispref = query.get_text();
                    # print "set prefix: %s" % self.hispref;
                self.hisidx = count-1;
            else:
                return gtk.TRUE;

        elif (event.keyval == self.AUTO_COMPLETE_KEY):
            # if we haven't grabbed a prefix as of yet, grab what we've got
            if (self.hispref == None):
                self.hispref = query.get_text();
                # print "set prefix: %s" % self.hispref;

            # if our history prefix has not yet been set, set it and start
            # scanning through the history
            if (self.hisidx == -1):
                self.hisidx = count;
            self.hisidx = self.history.match(self.hispref, self.hisidx);

        else:
            return gtk.FALSE;

        # if we found something scrolling or autocompleting, show it
        if (self.hisidx > -1 and self.hisidx < count):
            # print "displaying history entry %d." % self.hisidx;
            text = self.history.history[self.hisidx];
            self.set_query(query, text);
            return gtk.TRUE;

        # otherwise go back to whatever we were editing
        if (self.hispref != None):
            # print "restoring prefix %s" % self.hispref;
            self.set_query(query, self.hispref);

        # we always want to claim to have handled the keypress if it was
        # the autocomplete key, otherwise GTK will move the focus from our
        # query box widget
        if (event.keyval == self.AUTO_COMPLETE_KEY):
            return gtk.TRUE
        else:
            return gtk.FALSE;

    def set_query (self, query, text):
        # display text in the query box and select it for easy erasal
        query.set_text(text);
        query.select_region(0, len(text));

    def on_query_key_press_event (self, query, event):
        # handle history browsing and auto-completion
        if (self.handle_special_keys(query, event) == gtk.TRUE):
            return gtk.TRUE;

        # clear out our history prefix because it's in the text field now
        # and we'll grab it again if we need it
        self.hispref = None;
        self.hisidx = -1;

        # if they pressed return, map that to a special binding
        if (event.keyval == GDK.Return):
            binding = bindings.Binding("", bindings.Binding.URL, "", "%T");

        else:
            # ignore plain or shifted keystrokes
            if (event.state <= 1):
                return gtk.FALSE;
            # convert the key press into a string
            keystr = keyval_util.convert_keyval_state_to_string(
                event.keyval, event.state);
            # look up a binding for that key string
            binding = self.bindings.get_match(keystr);

        # if we found one, invoke it
        if (binding != None):
            text = query.get_text();
            binding.invoke(text);
            # append this entry to our history
            self.history.append(text);
            # and either bail or get ready for the next go
            if (self.appletMode):
                query.set_text("");
            else:
                gtk.mainquit();
            return gtk.TRUE;

        else:
            # otherwise, let GTK know that we didn't handle the key press
            return gtk.FALSE;

    def url_p (self, text):
        return (re.match("^http:", text) or
                re.match("^ftp:", text) or
                re.match("^https:", text) or
                re.match("^file:", text));

    def on_query_selection_received (self, query, selection_data, data):
        if (selection_data.length < 0):
            # print "Selection retrieval failed.";
            return;

        if (selection_data.type != GDK.SELECTION_TYPE_STRING):
            print "Selection target not returned as a string.";
            return;

        # print "Got selection '%s'." % selection_data.data;

        # prune spaces from the end of the text
        text = string.strip(selection_data.data);
        # compress whitespace (and convert newlines to spaces)
        text = re.sub("[ \r\n]+", " ", text);

        # make sure some selection was provided
        if (len(text) > 0):
            # check to see if we're looking at a url here
            is_url = self.url_p(text);

            # if this is a URL, we want to eat whitespace
            if (is_url):
                text = re.sub(" ", "", text);

            if (cmp(text, selection_data.data)):
                # print "Setting trimmed text '" + text  + "'.";
                query.set_text(text);

            query.select_region(0, len(text));
            # print "Selected from %d to %d." % (0, len(text));

    def exit_lookuplet (self, button):
        gtk.mainquit();
