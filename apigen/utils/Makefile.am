#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include $(top_srcdir)/make_rules

bin_SCRIPTS		= sdf2-to-adt adt-to-c

lib_LIBRARIES		= libADT.a
include_HEADERS		= ADT.h ADT_dict.h ADT-utils.h

libADT_a_SOURCES	= ADT.c ADT_dict.c ADT-utils.c

CLEANFILES		= libADT.a ADT.[ch] ADT.dict ADT_dict.[ch]

%INTERMEDIATE: ADT.c ADT_dict.c ADT.dict

ADT.c ADT.h ADT.dict ADT_dict.c ADT_dict.h: ADT.adt ADT.pro
	${top_srcdir}/utils/adt-to-c ADT -prologue ADT.pro -prefix ADT
