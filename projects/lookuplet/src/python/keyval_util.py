#
# $Id: keyval_util.py,v 1.2 2002/03/19 00:22:31 mdb Exp $
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
import GDK

# these are used when doing our conversion; we specifically only care
# about Control, Shift and Mod1; if you want to use other keys in your
# combinations, we don't want you drinking our soda
_CODES = [ GDK.CONTROL_MASK, GDK.SHIFT_MASK, GDK.MOD1_MASK ];
_NAMES = [ "Control", "Shift", "Mod1" ];

#
# Converts a (keyval, state) pair to a human readable string.
#
def convert_keyval_state_to_string (keyval, state):
    # bail if we've got an invalid kesym
    if (keyval == 0):
	return "None";

    # start with the empty string
    modstr = "";

    # we'd like to ask GDK to convert the key itself to a string, but they
    # don't wrap those functions. dooh!
    # key = GDK.keyval_name(keyval);

    # so we do this hack instead
    if (keyval < 0 or keyval > 255):
        return "None";
    key = string.lower(chr(keyval));

    # we have to handle the modifiers ourselves
    i = 0;
    for code in _CODES:
        if (state & code):
            modstr += _NAMES[i];
            modstr += "-";
        i += 1;

    return modstr + key;
