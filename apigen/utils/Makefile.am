#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include $(top_srcdir)/make_rules

bin_SCRIPTS = adt-to-c adt-to-java

bin_PROGRAMS = concat-adt convert-old-adt

concat_adt_SOURCES = concat-adt.c
concat_adt_LDADD   = ${top_builddir}/lib/libADT.a \
		     -L${ATERM}/lib -lATerm

convert_old_adt_SOURCES = convert-old-adt.c
convert_old_adt_LDADD   = ${top_builddir}/lib/libADT.a \
			  ${top_builddir}/old-lib/libOLDADT.a \
		     -L${ATERM}/lib -lATerm

INCLUDES = -I$(top_srcdir)/lib -I$(top_srcdir)/old-lib -I${ATERM}/include

