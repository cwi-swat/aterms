#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include $(top_srcdir)/make_rules

ADT_TO_C		= ${top_builddir}/utils/adt-to-c
noinst_LIBRARIES	= libOLDADT.a
noinst_HEADERS		= OLDADT.h OLDADT_dict.h 

libOLDADT_a_SOURCES	= OLDADT.c OLDADT_dict.c
CLEANFILES		= libOLDADT.a OLDADT.[ch] OLDADT.dict OLDADT_dict.[ch]
EXTRA_DIST		= OLDADT.adt OLDADT.pro

%INTERMEDIATE: OLDADT.c OLDADT_dict.c OLDADT.dict

OLDADT.c OLDADT.h OLDADT.dict OLDADT_dict.c OLDADT_dict.h: OLDADT.adt OLDADT.pro
	${ADT_TO_C} \
		--input OLDADT.adt		\
		--prologue OLDADT.pro	\
		--prefix OLDADT_		\
		--classpath ${top_builddir}:${EXTERNAL_JARS}
