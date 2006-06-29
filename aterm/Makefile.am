SUBDIRS = aterm utils test  doc
DIST_SUBDIRS = $(SUBDIRS)

EXTRA_DIST = \
	aterm.m4 \
       	init-package.m4 \
	package \
	aterm.spec \
	aterm.spec.in \
	aterm.pc.in

noinst_DATA = $(PACKAGE)-$(VERSION).pkg
CLEANFILES  = $(PACKAGE)-$(VERSION).pkg

pkgconfigdir=$(libdir)/pkgconfig
pkgconfig_DATA = aterm.pc

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $(top_srcdir)/package $@

ACLOCAL_AMFLAGS = -I .
EXTRA_DIST+=Doxyfile.src  

if MAINTAINER_MODE

CLEANFILES+=Doxyfile

.PHONY: doc

doc: Doxyfile 
	doxygen

Doxyfile: Doxyfile.src
	sed 's@__PACKAGE__@${PACKAGE}@g' < $< > $@

endif
