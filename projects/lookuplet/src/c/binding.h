/**
 * $Id: binding.h,v 1.2 2001/08/16 20:25:09 mdb Exp $
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

#ifndef _BINDING_H_
#define _BINDING_H_

#include <glib.h>

#define MAX_BINDING_LENGTH 250

typedef enum _LkBindingType LkBindingType;
typedef struct _LkBinding LkBinding;

enum _LkBindingType
{
    URL = 0,
    EXEC = 1
};

struct _LkBinding
{
    gchar*        key;
    LkBindingType type;
    gchar*        name;
    gchar*        argument;
};

#define LK_BINDING(b) ((LkBinding*)b)

extern LkBinding*
lk_binding_new (void);

extern LkBinding*
lk_binding_new_with_values (const gchar* key, LkBindingType type,
			    const gchar* name, const gchar* argument);

extern void
lk_binding_destroy (LkBinding* binding);

extern void
lk_binding_init (LkBinding* binding, const gchar* key,
		 LkBindingType type, const gchar* name, const gchar* argument);

extern void
lk_binding_load_bindings (GPtrArray* bindings);

extern void
lk_binding_save_bindings (GPtrArray* bindings);

#endif /* _BINDING_H_ */
