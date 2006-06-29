# $Id$

include $(top_srcdir)/java_make_rules

SUBDIRS = . test config doc

ACLOCAL_FLAGS = -I .

JARFILE = aterm-${VERSION}.jar
JAVA_PACKAGES=aterm,aterm.pure,aterm.test

EXTRA_DIST = package build.xml ${JARFILE}

pkgdata_DATA = ${JARFILE} 


CLEANFILES = aterm-java-${VERSION}.pkg

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $< $@
