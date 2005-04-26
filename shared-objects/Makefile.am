# $Id$

include $(top_srcdir)/java_make_rules

ACLOCAL_FLAGS = -I .

TOOLNAME = shared-objects
PACKAGE = shared
JARFILE = $(TOOLNAME)-$(VERSION).jar

bin_SCRIPTS = $(TOOLNAME)-config
pkgdata_DATA = $(JARFILE)
noinst_SCRIPTS = $(TOOLNAME)-$(VERSION).pkg

EXTRA_DIST = package build.xml $(JARFILE)

CLEANFILES = \
	$(TOOLNAME)-$(VERSION).pkg \
	$(TOOLNAME)-config

$(TOOLNAME)-$(VERSION).pkg: package Makefile
	cp $< $@

$(TOOLNAME)-config: Makefile
	echo "#! /bin/sh" > $@
	echo "echo $(pkgdatadir)/$(JARFILE)" >> $@
	chmod +x $@
