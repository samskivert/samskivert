/**
 * $Id: preferences.h,v 1.2 2001/02/24 02:45:36 mdb Exp $
 */

#ifndef _PREFERENCES_H_
#define _PREFERENCES_H_

#include <gnome.h>

extern void
lk_prefs_init (void);

extern GPtrArray*
lk_prefs_get_bindings (void);

extern void
lk_prefs_cleanup (void);

extern void
lk_prefs_display (void);

#endif /* _PREFERENCES_H_ */
