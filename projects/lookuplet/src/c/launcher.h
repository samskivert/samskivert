/**
 * $Id: launcher.h,v 1.1 2000/12/10 23:38:39 mdb Exp $
 */

#ifndef _LAUNCHER_H_
#define _LAUNCHER_H_

#include <glib.h>

extern void
lookuplet_launcher_init (void);

extern void
lookuplet_launcher_cleanup (void);

extern gboolean
lookuplet_launcher_launch (const char* terms, const char* qualifier);

#endif /* _LAUNCHER_H_ */
