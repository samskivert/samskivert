#
# $Id: Makefile.am,v 1.1 2000/12/10 23:38:39 mdb Exp $

AUTOMAKE_OPTIONS = no-dependencies

INCLUDES = \
	$(GNOME_INCLUDEDIR)

bin_PROGRAMS = lookuplet
bin_DIR = ../install.d/

lookuplet_SOURCES = \
	launcher.c \
	lookuplet.c \
	querybox.c

lookuplet_LDADD = \
	$(GNOME_LIBDIR) \
	$(GTK_LIBS) \
	$(GNOMEGNORBA_LIBS) \
	-lpanel_applet
