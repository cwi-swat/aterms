
include $(top_srcdir)/java_make_rules

SUBDIRS = . scripts lib-1.0 lib-2.0 lib-3.0 utils test config

ACLOCAL_FLAGS = -I .

JARFILE    = apigen.jar
pkgdata_DATA  = ${JARFILE}

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

EXTRA_DIST+=Doxyfile.src  

if MAINTAINER_MODE

CLEANFILES+=Doxyfile

.PHONY: doc

doc: Doxyfile 
	doxygen

Doxyfile: Doxyfile.src
	sed 's@__PACKAGE__@${PACKAGE}@g' < $< > $@

endif
