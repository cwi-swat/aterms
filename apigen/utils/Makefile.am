#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include $(top_srcdir)/make_rules

bin_SCRIPTS = adt-to-c adt-to-java

bin_PROGRAMS = concat-adt upgrade-adt

concat_adt_SOURCES = concat-adt.c
concat_adt_LDADD   = ${top_builddir}/lib-2.0/libADT.a \
		     -L${ATERM}/lib -lATerm

upgrade_adt_SOURCES = upgrade-adt.c
upgrade_adt_LDADD   = ${top_builddir}/lib-2.0/libADT.a \
			  ${top_builddir}/lib-1.0/libADT10.a \
		     -L${ATERM}/lib -lATerm

INCLUDES = -I$(top_srcdir)/lib -I$(top_srcdir)/lib-1.0 -I${ATERM}/include

