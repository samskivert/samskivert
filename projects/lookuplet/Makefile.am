#
# $Id: Makefile.am,v 1.1 2000/12/10 23:38:39 mdb Exp $

SUBDIRS = src

# Edit these target directories if they differ on your system. If you
# change anything, run "autoremake", then continue with installation as
# usual.

BINDIR = /usr/bin
DESKTOPDIR = /usr/share/applets/Network
CORBADIR = /etc/CORBA/servers
PIXMAPDIR = /usr/share/pixmaps

install:
	$(INSTALL_PROGRAM) src/lookuplet $(BINDIR)/lookuplet
	$(INSTALL_PROGRAM) src/lookuplet.gnorba $(CORBADIR)/lookuplet.gnorba
	$(INSTALL_PROGRAM) src/lookuplet.desktop $(DESKTOPDIR)/lookuplet.desktop
	$(INSTALL_PROGRAM) src/gnome-lookuplet.xpm $(PIXMAPDIR)/gnome-lookuplet.xpm

uninstall:
	rm \
		$(BINDIR)/lookuplet \
		$(CORBADIR)/lookuplet.gnorba \
		$(DESKTOPDIR)/lookuplet.desktop \
		$(PIXMAPDIR)/gnome-lookuplet.xpm

clean:
	rm src/lookuplet
