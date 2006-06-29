
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

JAVA_PACKAGES=apigen,apigen.adt,apigen.adt.api,apigen.adt.api.types,apigen.adt.api.types.type,apigen.adt.api.types.module,apigen.adt.api.types.modulename,apigen.adt.api.types.separator,apigen.adt.api.types.entry,apigen.gen,apigen.gen.tom,apigen.gen.tom.java,apigen.gen.tom.c,apigen.gen.java,apigen.gen.c,apigen.util
