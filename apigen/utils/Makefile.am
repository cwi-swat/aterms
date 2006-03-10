# $Id$

bin_PROGRAMS = concat-adt upgrade-adt

if MAINTAINER_MODE
ATERM_LIB = ATerm-dbg
else
ATERM_LIB = ATerm
endif

concat_adt_SOURCES = concat-adt.c
concat_adt_LDADD = \
	${top_builddir}/lib-2.0/libADT.a \
	-L${ATERM}/lib -l$(ATERM_LIB)

upgrade_adt_SOURCES = upgrade-adt.c
upgrade_adt_LDADD = \
	${top_builddir}/lib-2.0/libADT.a \
	${top_builddir}/lib-1.0/libADT10.a \
	-L${ATERM}/lib -l$(ATERM_LIB)

INCLUDES = \
	-I$(top_srcdir)/lib \
	-I$(top_srcdir)/lib-1.0 \
	-I$(top_srcdir)/lib-2.0 \
	-I${ATERM}/include
