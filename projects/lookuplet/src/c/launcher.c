/**
 * $Id: launcher.c,v 1.1 2000/12/10 23:38:39 mdb Exp $
 */

#include <string.h>
#include <ctype.h>

#include <config.h>
#include <glib.h>
#include <gnome.h>

#include "launcher.h"

#define DEFAULT_QUERY "http://search.metacrawler.com/crawler?general=%U"

/**
 * This hashtable stores the mappings from qualifier keys to URL templates
 * for use when we launch a URL for a particular set of terms.
 */
static GHashTable* _qualmap;

/**
 * Removes and frees any previous entry registered with the supplied key
 * before inserting the new key/value pair.
 */
static void
g_hash_table_insert_safe (GHashTable* table, gchar* key, gchar* value)
{
    gpointer oldkey, oldvalue;

    if (g_hash_table_lookup_extended(table, key, &oldkey, &oldvalue)) {
	g_free(oldkey);
	g_free(oldvalue);
    }

    g_hash_table_insert(table, key, value);
}

/**
 * Loads up the mappings for the launcher from the configuration
 * repository.
 */
void
lookuplet_launcher_init (void)
{
    _qualmap = g_hash_table_new(g_str_hash, g_str_equal);

    /* for now, just insert some bogus config parameters */
    g_hash_table_insert_safe(_qualmap,
			     g_strdup("Ctrl-s"), g_strdup(DEFAULT_QUERY));
}

static void
free_table_entry (gpointer key, gpointer value, gpointer rock)
{
    g_free(key);
    g_free(value);
}

/**
 * Frees the mapping information used by the launcher.
 */
void
lookuplet_launcher_cleanup (void)
{
    g_hash_table_foreach(_qualmap, free_table_entry, NULL);
    g_hash_table_destroy(_qualmap);
    _qualmap = NULL;
}

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
 * Displays a URL with the supplied terms appropriately embedded. The URL
 * shown depends on the supplied qualifier. Each qualifier is mapped to a
 * particular URL into which the terms are substituted before loading.
 *
 * @return false if no qualifier mapping could be found for the supplied
 * qualifier.
 */
gboolean
lookuplet_launcher_launch (const char* terms, const char* qualifier)
{
    gchar* uterms;
    gchar* template;

    uterms = url_encode(terms);

    /* if no qualifier was supplied, we use the default template;
     * otherwise we get the template from the mapping table */
    if (qualifier == NULL) {
	template = DEFAULT_QUERY;
    } else {
	template = g_hash_table_lookup(_qualmap, qualifier);
    }

    /* if we found a template, construct the actual URL from it by
     * replacing instances of %T with the terms and %U with the URL
     * encoded terms. */
    if (template != NULL) {
	GString* url = g_string_new(template);
	/* first replace %U because we know that won't result in any '%T's
	 * being inserted into the string because the url encoded terms
	 * encode all %s */
	g_string_replace(url, "%U", uterms);
	g_string_replace(url, "%T", terms);
	gnome_url_show(url->str);
	g_string_free(url, TRUE);
    }

    g_free(uterms);

    return TRUE;
}
