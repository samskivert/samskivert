#
# $Id: history.py,v 1.1 2002/03/17 21:25:20 mdb Exp $
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

import gnome.config

class History:
    "Used to persistently maintain a history of entries for the \
    lookuplet query text field."

    # the maximum history size
    HISTORY_SIZE = 10;

    # the actual history entries
    history = [];

    #
    # Loads up the history from persistent preferences and prepares for
    # operation.
    #
    def __init__ (self):
        gnome.config.push_prefix("/lookuplet/");
        count = gnome.config.get_int("lookuplet/history/count");
        for h in range(0, count):
            self.history.append(gnome.config.get_string(
                "lookuplet/history/entry_%.2u" % h));
            # print "loaded %d: %s" % (h, self.history[h]);
        gnome.config.pop_prefix();
        return;

    #
    # Adds an entry to the history.
    #
    def append (self, entry):
        # if this entry is already in the history list, we simply want to
        # move it to the end
        if (entry in self.history):
            self.history.remove(entry);

        # add our entry
        self.history.append(entry);

        # trim the history if it's too big
        if (len(self.history) > self.HISTORY_SIZE):
            self.history.pop(0);

        # flush our config to the configuration repository
        gnome.config.push_prefix("/lookuplet/");
        count = len(self.history);
        gnome.config.set_int("lookuplet/history/count", count);
        for h in range(0, count):
            gnome.config.set_string(
                "lookuplet/history/entry_%.2u" % h, self.history[h]);
            # print "wrote %d: %s" % (h, self.history[h]);
        gnome.config.sync();
        gnome.config.drop_all();
        gnome.config.pop_prefix();

    #
    # Scans back from one before the specified index looking for a history
    # entry that matches the specified prefix, returning its index if one
    # is found or -1 if one is not.
    #
    def match (self, prefix, index):
        count = len(self.history);
        for offset in range(1, count+1):
            # do some funny math to always ensure a positive value
            pos = (index - offset + count) % count;
            # print "scanning %s for %s." % (self.history[pos], prefix);
            if (self.history[pos].find(prefix) == 0):
                # print "found it at %d." % pos;
                return pos;
        # print "didn't find it.";
        return -1;
