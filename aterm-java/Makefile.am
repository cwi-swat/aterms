# $Id$

include $(top_srcdir)/java_make_rules

SUBDIRS = . test config 

ACLOCAL_FLAGS = -I .

JARFILE = aterm-${VERSION}.jar

EXTRA_DIST = package build.xml ${JARFILE}

pkgdata_DATA = ${JARFILE} 


CLEANFILES = aterm-java-${VERSION}.pkg

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $< $@
