#
# $Id: Makefile,v 1.1 2001/02/13 05:54:29 mdb Exp $
#
# A Makefile for generating docs for the samskivert stuff

ROOT = ../src/java

PKGDIR = $(ROOT)/com/samskivert

PACKAGES = $(shell find $(PKGDIR) -name '*.java' | sed 's:/[^/]*$$::g' | sort -u | sed 's:^$(ROOT)/::g' | sed 's:/:.:g' | grep -v '^build$$')

DOCDIR = .

all: docs

include $(ROOT)/build/Makefile.java
