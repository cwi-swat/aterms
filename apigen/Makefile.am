
include $(top_srcdir)/java_make_rules

SUBDIRS = . scripts lib-1.0 lib-2.0 lib-3.0 utils test doc config

ACLOCAL_FLAGS = -I .

JARFILE    = apigen.jar
data_DATA  = ${JARFILE}

CLEANFILES = \
	     ${PACKAGE}-*.pkg

EXTRA_DIST = \
	     ${JARFILE} \
	     java_make_rules \
	     build.xml \
	     package

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg 

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $< $@

