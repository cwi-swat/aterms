#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include $(top_srcdir)/make_rules

ADT_TO_C		= ${top_builddir}/utils/adt-to-c
noinst_LIBRARIES	= libADT10.a
noinst_HEADERS		= ADT10.h ADT10_dict.h 

libADT10_a_SOURCES	= ADT10.c ADT10_dict.c
CLEANFILES		= libADT10.a ADT10.[ch] ADT10.dict ADT10_dict.[ch]
EXTRA_DIST		= ADT10.adt ADT10.pro

%INTERMEDIATE: ADT10.c ADT10_dict.c ADT10.dict

ADT10.c ADT10.h ADT10.dict ADT10_dict.c ADT10_dict.h: ADT10.adt ADT10.pro
	${ADT_TO_C} \
		--input ADT10.adt		\
		--prologue ADT10.pro	\
		--prefix ADT10_		\
		--classpath ${top_builddir}:${EXTERNAL_JARS}
