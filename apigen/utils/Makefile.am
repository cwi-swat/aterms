#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include $(top_srcdir)/make_rules

bin_SCRIPTS		= adt-to-c adt-to-java

bin_PROGRAMS = concat-adt

concat_adt_SOURCES = concat-adt.c
concat_adt_LDADD   = -L${top_builddir}/lib -lADT \
		     -L${ATERM}/lib -lATerm

INCLUDES = -I$(top_srcdir)/lib -I${ATERM}/include

