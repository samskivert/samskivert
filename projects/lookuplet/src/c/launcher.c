/**
 * $Id: launcher.c,v 1.3 2001/08/16 20:25:09 mdb Exp $
 * 
 * lookuplet - a utility for quickly looking up information
 * Copyright (C) 2001 Michael Bayne
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

#include <string.h>
#include <ctype.h>

#include <config.h>
#include <glib.h>
#include <gnome.h>

#include "launcher.h"

#define DEFAULT_COMMAND "%T"
#define DEFAULT_TYPE URL

#define ESC_CHAR '%'

static const gchar*
escape (gchar value)
{
    static char buffer[] = "%xx";
    static char* xlate = "0123456789ABCDEF";
    buffer[0] = ESC_CHAR;
    buffer[1] = xlate[(value >> 4) & 0xF];
    buffer[2] = xlate[value & 0xF];
    return buffer;
}

/**
 * URL encodes the supplied text. The result must be freed by the caller
 * using g_free().
 */
static gchar*
url_encode (const gchar* text)
{
    GString* result;
    guint tlen, i;
    gchar* retval;

    tlen = strlen(text);
    result = g_string_sized_new(tlen);

    for (i = 0; i < tlen; i++) {
	if (!isalnum(text[i]) || text[i] == ESC_CHAR) {
	    g_string_append(result, escape(text[i]));
	} else {
	    g_string_append_c(result, text[i]);
	}
    }

    retval = result->str;
    g_string_free(result, FALSE);

    return retval;
}

/**
 * Replaces all instances of before with after in the supplied source
 * string.
 *
 * @return the number of replacements made.
 */
static
guint g_string_replace (GString* source, const gchar* before,
			const gchar* after)
{
    gchar* pos;
    gchar* spot;
    guint beflen = strlen(before);
    guint aftlen = strlen(after);
    guint replacements = 0;

    /* sanity checks */
    g_return_val_if_fail(source != NULL, 0);
    g_return_val_if_fail(before != NULL, 0);
    g_return_val_if_fail(after != NULL, 0);
    g_return_val_if_fail(beflen > 0, 0);

    pos = source->str;
    while ((spot = strstr(pos, before)) != NULL) {
	replacements++;
	/* erase the match and insert the new stuff */
	g_string_erase(source, spot-source->str, beflen);
	g_string_insert(source, spot-source->str, after);
	/* move the pointer on up */
	pos += spot-source->str + aftlen;
    }

    return replacements;
}

#define TOKEN_START '%'
#define TERM_TOKEN 'T'
#define ENCODED_TERM_TOKEN "U"

/**
 * Either displays a URL or invokes an command line with the supplied
 * terms appropriately embedded.
 */
void
lk_launcher_launch (const LkBinding* binding, const gchar* terms)
{
    char cwdbuf[1024];
    GString* cmd;
    gchar* uterms, *command;
    LkBindingType type;

    /* figure out what we're supposed to do */
    if (binding == NULL) {
	command = DEFAULT_COMMAND;
	type = DEFAULT_TYPE;

    } else {
	command = binding->argument;
	type = binding->type;
    }

    /* construct the actual command from it by replacing instances of %T
     * with the terms and %U with the URL encoded terms. */
    cmd = g_string_new(command);
    /* first replace %U because we know that won't result in any '%T's
     * being inserted into the string because the url encoded terms
     * encode all %s */
    uterms = url_encode(terms);
    g_string_replace(cmd, "%U", uterms);
    g_free(uterms);
    g_string_replace(cmd, "%T", terms);

    /* now we either invoke a program or launch a URL */
    switch (type) {
    case EXEC:
	gnome_execute_shell(getcwd(cwdbuf, 1024), cmd->str);
	break;

    default:
    case URL:
	gnome_url_show(cmd->str);
	break;
    }

    g_string_free(cmd, TRUE);
}
