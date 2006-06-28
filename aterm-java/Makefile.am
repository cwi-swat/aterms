# $Id$

include $(top_srcdir)/java_make_rules

SUBDIRS = . test config doc

ACLOCAL_FLAGS = -I .

JARFILE = aterm-${VERSION}.jar

EXTRA_DIST = package build.xml ${JARFILE}

pkgdata_DATA = ${JARFILE} 


CLEANFILES = aterm-java-${VERSION}.pkg

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $< $@

if MAINTAINER_MODE

.PHONY: doc

doc:
	${ANT_ENV} ant doc; \
		cd doc && $(MAKE) $(AM_MAKE_FLAGS) doc


endif
