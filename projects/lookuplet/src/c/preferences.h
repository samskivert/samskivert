/**
 * $Id: preferences.h,v 1.1 2001/02/24 02:35:20 mdb Exp $
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
lk_prefs_display ();

#endif /* _PREFERENCES_H_ */
