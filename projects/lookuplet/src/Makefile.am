#
# $Id: Makefile.am,v 1.3 2001/08/16 00:12:11 mdb Exp $

bin_PROGRAMS = lookuplet

lookuplet_SOURCES = \
	lookuplet.c \
	launcher.c \
	querybox.c \
	preferences.c \
	binding.c \
	keysym-util.c

INCLUDES = 						\
	-I.						\
	-DGNOMELOCALEDIR=\""$(datadir)/locale"\"	\
	-I$(top_srcdir)					\
	$(GNOME_INCLUDEDIR)

lookuplet_LDADD = \
	$(GNOME_LIBDIR) \
	$(GNOMEUI_LIBS) \
	$(INTLLIBS) 

# currently the installation of these bitmaps wouldn't be necessary
bitmapsdir = $(datadir)/pixmaps
bitmaps_DATA = gnome-lookuplet.xpm

EXTRA_DIST = \
	lookuplet.desktop \
	lookuplet.gnorba \
	$(bitmaps_DATA)

sysdir = $(datadir)/applets/Network
sys_DATA = lookuplet.desktop

gnorbadir  = $(sysconfdir)/CORBA/servers
gnorba_DATA = lookuplet.gnorba
