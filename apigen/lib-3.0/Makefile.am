# $Id$

INCLUDES = \
	-I$(ATERM)/include \
	-I$(PT_SUPPORT)/include

ADT_TO_C = ${top_builddir}/scripts/adt-to-c-test
lib_LIBRARIES = libADT.a
include_HEADERS = ADT.h ADT_dict.h

libADT_a_SOURCES = ADT.c ADT_dict.c
CLEANFILES = libADT.a ADT.[ch] ADT.dict ADT_dict.[ch]
EXTRA_DIST = ADT.adt 

%INTERMEDIATE: ADT.c ADT_dict.c ADT.dict

ADT.c ADT.h ADT.dict ADT_dict.c ADT_dict.h: ADT.adt 
	${ADT_TO_C} \
		--input ADT.adt \
		--name ADT \
		--prefix ADT_
