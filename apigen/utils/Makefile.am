# $Id$

bin_SCRIPTS = adt-to-c adt-to-java
noinst_SCRIPTS = adt-to-java-test

bin_PROGRAMS = concat-adt upgrade-adt

EXTRA_DIST = $(wildcard *.src)

concat_adt_SOURCES = concat-adt.c
concat_adt_LDADD = \
	${top_builddir}/lib-2.0/libADT.a \
	-L${ATERM}/lib -lATerm

upgrade_adt_SOURCES = upgrade-adt.c
upgrade_adt_LDADD = \
	${top_builddir}/lib-2.0/libADT.a \
	${top_builddir}/lib-1.0/libADT10.a \
	-L${ATERM}/lib -lATerm

INCLUDES = \
	-I$(top_srcdir)/lib \
	-I$(top_srcdir)/lib-1.0 \
	-I$(top_srcdir)/lib-2.0 \
	-I${ATERM}/include

ABS_TOP_BUILDDIR=$(shell cd ${top_builddir}; pwd)

%: %.src Makefile
	sed -e 's#__JAVA_ATERM__#${JAVA_ATERM}#g' \
		-e 's#__APIGEN_CLASSES__#${prefix}/classes#g' < $< > $@ \
	&& chmod +x $@

%-test: %.src Makefile
	sed -e 's#__JAVA_ATERM__#${JAVA_ATERM}#g' \
		-e 's#__APIGEN_CLASSES__#${ABS_TOP_BUILDDIR}#g' < $< > $@ \
	&& chmod +x $@
