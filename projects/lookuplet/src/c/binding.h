/**
 * $Id: binding.h,v 1.1 2001/02/24 02:35:20 mdb Exp $
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
