#
# $Id: Makefile.am,v 1.2 2001/02/24 02:35:20 mdb Exp $

INCLUDES = 						\
	-I.						\
	-I$(srcdir)					\
	-I$(top_srcdir)/intl -I$(top_builddir)/intl	\
	-DGNOMELOCALEDIR=\""$(datadir)/locale"\"	\
	-I$(includedir) $(GNOME_INCLUDEDIR)

bin_PROGRAMS = lookuplet

lookuplet_SOURCES = \
	lookuplet.c \
	launcher.c \
	querybox.c \
	preferences.c \
	binding.c \
	keysym-util.c

lookuplet_LDADD = \
	$(GNOME_LIBDIR) \
	$(GNOME_APPLETS_LIBS) \
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
