# $Id$

INCLUDES = \
	-I$(ATERM)/include \
	-I$(PT_SUPPORT)/include

ADT_TO_C = ${top_builddir}/utils/adt-to-c-test
noinst_LIBRARIES = libADT10.a
noinst_HEADERS = ADT10.h ADT10_dict.h 

libADT10_a_SOURCES = ADT10.c ADT10_dict.c
CLEANFILES = libADT10.a ADT10.[ch] ADT10.dict ADT10_dict.[ch]
EXTRA_DIST = ADT10.adt ADT10.pro

%INTERMEDIATE: ADT10.c ADT10_dict.c ADT10.dict

ADT10.c ADT10.h ADT10.dict ADT10_dict.c ADT10_dict.h: ADT10.adt ADT10.pro
	${ADT_TO_C} \
		--input ADT10.adt \
		--name ADT10 \
		--prologue ADT10.pro \
		--prefix ADT10_
