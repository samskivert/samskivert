/**
 * $Id: launcher.h,v 1.2 2001/02/24 02:35:20 mdb Exp $
 */

#ifndef _LAUNCHER_H_
#define _LAUNCHER_H_

#include <glib.h>

#include "binding.h"

extern gboolean
lk_launcher_launch (const LkBinding* binding, const gchar* terms);

#endif /* _LAUNCHER_H_ */
