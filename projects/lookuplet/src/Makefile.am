#
# $Id: Makefile.am,v 1.7 2003/11/28 21:34:59 mdb Exp $

bin_PROGRAMS = lookuplet

lookuplet_SOURCES = \
	lookuplet_glade.cc \
	lookuplet.cc \
	binding_glade.cc \
	binding.cc \
	about_glade.cc \
	about.cc \
	properties_glade.cc \
	properties.cc 

noinst_HEADERS = \
	lookuplet_glade.hh \
	lookuplet.hh \
	binding_glade.hh \
	binding.hh \
	about_glade.hh \
	about.hh \
	properties_glade.hh \
	properties.hh 

CXXFLAGS = @CXXFLAGS@ @GNOMEMM_CFLAGS@
LDFLAGS= @LDFLAGS@ 

lookuplet_LDADD = @LIBS@ @GNOMEMM_LIBS@
