/**
 * $Id: binding.c,v 1.1 2001/02/24 02:35:20 mdb Exp $
 */

#include <gnome.h>
#include "binding.h"

static gchar* DEFAULT_BINDING_KEYS[] =
{
    "Control-m",
    "Control-g",
    "Control-d",
    "Control-Shift-d",
    "Control-f",
};

static LkBindingType DEFAULT_BINDING_TYPES[] =
{
    URL,
    URL,
    EXEC,
    URL,
    URL,
};

static gchar* DEFAULT_BINDING_NAMES[] =
{
    "MetaCrawler search",
    "Google search",
    "Dictionary lookup",
    "Debian package search",
    "Freshmeat search",
};

static gchar* DEFAULT_BINDING_ARGUMENTS[] =
{
    "http://search.metacrawler.com/crawler?general=%U",
    "http://www.google.com/search?client=googlet&q=%U",
    "gdict -a %T",
    "http://cgi.debian.org/cgi-bin/search_contents.pl?word=%U&case=insensitive&version=unstable&arch=i386&directories=yes",
    "http://freshmeat.net/search/?q=%U",
};

#define DEFAULT_BINDING_COUNT \
(sizeof(DEFAULT_BINDING_TYPES)/sizeof(LkBindingType))

LkBinding*
lk_binding_new (void)
{
    LkBinding* binding = (LkBinding*)g_malloc(sizeof(LkBinding));
    binding->key = g_strdup("");
    binding->name = g_strdup("");
    binding->argument = g_strdup("");
    return binding;
}

LkBinding*
lk_binding_new_with_values (const gchar* key, LkBindingType type,
			    const gchar* name, const gchar* argument)
{
    LkBinding* binding = (LkBinding*)g_malloc(sizeof(LkBinding));
    binding->key = g_strdup(key);
    binding->type = type;
    binding->name = g_strdup(name);
    binding->argument = g_strdup(argument);
    return binding;
}

void
lk_binding_destroy (LkBinding* binding)
{
    g_free(binding->key);
    g_free(binding->name);
    g_free(binding->argument);
    g_free(binding);
}

void
lk_binding_init (LkBinding* binding, const gchar* key,
		 LkBindingType type, const gchar* name, const gchar* argument)
{
    g_free(binding->key);
    binding->key = g_strdup(key);
    binding->type = type;
    g_free(binding->name);
    binding->name = g_strdup(name);
    g_free(binding->argument);
    binding->argument = g_strdup(argument);
}

static gchar*
config_get_string (const gchar* field, gint index)
{
    gchar gkey[100];
    gchar* value;

    /* construct the path */
    g_snprintf(gkey, sizeof(gkey),
	       "lookuplet/bindings/%s_%.2u", field, index);

    /* fetch the value and deal with non-existent values */
    value = gnome_config_get_string(gkey);
    if (value == NULL) {
	value = g_strdup("");
    }

    return value;
}

void
lk_binding_load_bindings (GPtrArray* bindings)
{
    gchar gkey[100];
    int count, i;

    gnome_config_push_prefix("/lookuplet/");
    count = gnome_config_get_int("lookuplet/bindings/count");

    /* if we have bindings defined, load those */
    if (count > 0) {
	for (i = 0; i < count; i++) {
	    gchar* key, *name, *argument;
	    LkBindingType type = URL;

	    key = config_get_string("key", i);
	    g_snprintf(gkey, sizeof(gkey), "lookuplet/bindings/type_%.2u", i);
	    type = (LkBindingType)gnome_config_get_int(gkey);
	    name = config_get_string("name", i);
	    argument = config_get_string("arg", i);

	    g_ptr_array_add(bindings, lk_binding_new_with_values(
		key, type, name, argument));

	    g_free(key);
	    g_free(name);
	    g_free(argument);
	}

    } else {
	/* otherwise use the default bindings */
	for (i = 0; i < DEFAULT_BINDING_COUNT; i++) {
	    LkBinding* binding =
		lk_binding_new_with_values(DEFAULT_BINDING_KEYS[i],
					   DEFAULT_BINDING_TYPES[i],
					   DEFAULT_BINDING_NAMES[i],
					   DEFAULT_BINDING_ARGUMENTS[i]);
	    g_ptr_array_add(bindings, binding);
	}
    }

    gnome_config_pop_prefix();
}

void
lk_binding_save_bindings (GPtrArray* bindings)
{
    gchar gkey[100];
    int i;

    gnome_config_push_prefix("/lookuplet/");

    /* save the total number of bindings */
    gnome_config_set_int("lookuplet/bindings/count", bindings->len);

    /* save each binding */
    for (i = 0; i < bindings->len; i++) {
	LkBinding* binding = LK_BINDING(g_ptr_array_index(bindings, i));

	g_snprintf(gkey, sizeof(gkey),
		   "lookuplet/bindings/key_%.2u", i);
	gnome_config_set_string(gkey, binding->key);

	g_snprintf(gkey, sizeof(gkey),
		   "lookuplet/bindings/type_%.2u", i);
	gnome_config_set_int(gkey, binding->type);

	g_snprintf(gkey, sizeof(gkey),
		   "lookuplet/bindings/name_%.2u", i);
	gnome_config_set_string(gkey, binding->name);

	g_snprintf(gkey, sizeof(gkey),
		   "lookuplet/bindings/arg_%.2u", i);
	gnome_config_set_string(gkey, binding->argument);
    }

    /* write our new settings to disk */
    gnome_config_sync();

    /* cargo cult programming! i dunno, the other guy did this too */
    gnome_config_drop_all();

    gnome_config_pop_prefix();
}
