#
# $Id: Makefile,v 1.2 2001/02/16 03:29:07 mdb Exp $
#
# A Makefile for generating docs for the samskivert stuff

ROOT = ../src/java

PKGDIR = $(ROOT)/com/samskivert

PACKAGES = $(shell find $(PKGDIR) -name '*.java' | sed 's:/[^/]*$$::g' | sort -u | sed 's:^$(ROOT)/::g' | sed 's:/:.:g' | grep -v '^build$$')

DOCDIR = .

include $(ROOT)/build/Makefile.java

# let make know that we want to build the docs in this directory by default
extra: docs
