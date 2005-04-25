# $Id$

include $(top_srcdir)/java_make_rules

SUBDIRS = config .

ACLOCAL_FLAGS = -I .

TOOLNAME = shared-objects
PACKAGE = shared
JARFILE = $(TOOLNAME)-$(VERSION).jar

pkgdata_DATA = ${JARFILE}
noinst_SCRIPTS = ${TOOLNAME}-${VERSION}.pkg

EXTRA_DIST = package build.xml ${JARFILE}

${TOOLNAME}-${VERSION}.pkg: package Makefile
	cp $< $@

CLEANFILES = ${PACKAGE}-${VERSION}.pkg
