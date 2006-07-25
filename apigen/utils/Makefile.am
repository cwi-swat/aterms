# $Id$

include $(top_srcdir)/Makefile.meta

bin_PROGRAMS = concat-adt upgrade-adt

concat_adt_SOURCES = concat-adt.c
concat_adt_LDADD = \
	${top_builddir}/lib-2.0/libADT.a \
	$(ATERM_LIBS) 

upgrade_adt_SOURCES = upgrade-adt.c
upgrade_adt_LDADD = \
	${top_builddir}/lib-2.0/libADT.a \
	${top_builddir}/lib-1.0/libADT10.a \
	$(ATERM_LIBS) 

INCLUDES = \
	-I$(top_srcdir)/lib \
	-I$(top_srcdir)/lib-1.0 \
	-I$(top_srcdir)/lib-2.0 \
	${ATERM_CFLAGS}
