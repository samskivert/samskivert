#
# $Id: bindings.py,v 1.2 2002/03/18 00:03:41 mdb Exp $
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

import posix
import re
import string
import urllib

import gnome.config
import gnome.url

class Binding:
    "Contains the configuration for a particular key binding and its \
    associated query mapping."

    # constants defining which type of binding this is
    URL = 0;
    EXEC = 1;

    # the key combination associated with this binding
    key = "";

    # the type of binding (URL or Exec)
    type = URL;

    # the human readable name of the binding
    name = "";

    # the URL or command line assosciated with the binding
    argument = "";

    #
    # Constructs a binding with its requisite parameters.
    #
    def __init__ (self, key, type, name, argument):
        self.key = key;
        self.type = type;
        self.name = name;
        self.argument = argument;

    def invoke (self, terms):
        command = re.sub("%T", terms, self.argument);
        command = re.sub("%U", urllib.quote(terms), command);
        if (self.type == self.EXEC):
            posix.system(command)
        else:
            gnome.url.show(command)

    def update (self, key, type, name, argument):
        modified = 0;
        if (cmp(key, self.key)):
            self.key = key;
            modified = 1;

        if (type != self.type):
            self.type = type;
            modified = 1;

        if (cmp(name, self.name)):
            self.name = name;
            modified = 1;

        if (cmp(argument, self.argument)):
            self.argument = argument;
            modified = 1;

        return modified;

    def to_string (self):
        return "[key=%s, type=%d, name=%s, arg=%s]" % (
            self.key, self.type, self.name, self.argument);

class BindingSet:
    "Maintains the set of bindings loading into the program."

    # the list of Binding objects
    bindings = [];

    #
    # The default constructor.
    #
    def __init__ (self):
        gnome.config.push_prefix("/lookuplet/");
        count = gnome.config.get_int("lookuplet/bindings/count");
        for b in range(0, count):
            key = gnome.config.get_string("lookuplet/bindings/key_%.2u" % b);
            type = gnome.config.get_int("lookuplet/bindings/type_%.2u" % b);
            name = gnome.config.get_string("lookuplet/bindings/name_%.2u" % b);
            arg = gnome.config.get_string("lookuplet/bindings/arg_%.2u" % b);
            self.bindings.append(Binding(key, type, name, arg));
        gnome.config.pop_prefix();

        # if we loaded no bindings, use the defaults
        if (count == 0):
            self.bindings.append(Binding(
                "Control-g", Binding.URL, "Google search",
                "http://www.google.com/search?client=googlet&q=%U"));
            self.bindings.append(Binding(
                "Control-d", Binding.EXEC, "Dictionary lookup",
                "gdict -a '%T'"));
            self.bindings.append(Binding(
                "Control-Shift-d", Binding.URL, "Debian package search",
                "http://cgi.debian.org/cgi-bin/search_contents.pl?word=%U" +
                "&case=insensitive&version=unstable&arch=i386" +
                "&directories=yes"));
            self.bindings.append(Binding(
                "Control-f", Binding.URL, "Freshmeat search",
                "http://freshmeat.net/search/?q=%U"));
            self.bindings.append(Binding(
                "Control-i", Binding.URL, "IMDB Title search",
                "http://www.imdb.com/Tsearch?title=%U&restrict=Movies+only"));
        return;

    #
    # Returns the binding with a keysym matching the specified keysym or
    # None if no binding matches.
    #
    def get_match (self, keysym):
        for binding in self.bindings:
            if (binding.key == keysym):
                return binding
        return None;

    #
    # Stores our updated bindings to our GConf preferences.
    #
    def flush (self):
        gnome.config.push_prefix("/lookuplet/");
        gnome.config.set_int("lookuplet/bindings/count", len(self.bindings));
        b = 0
        for binding in self.bindings:
            gnome.config.set_string(
                "lookuplet/bindings/key_%.2u" % b, binding.key);
            gnome.config.set_int(
                "lookuplet/bindings/type_%.2u" % b, binding.type);
            gnome.config.set_string(
                "lookuplet/bindings/name_%.2u" % b, binding.name);
            gnome.config.set_string(
                "lookuplet/bindings/arg_%.2u" % b, binding.argument);
            b += 1;
        gnome.config.sync();
        gnome.config.drop_all();
        gnome.config.pop_prefix();
        return;
