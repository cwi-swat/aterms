# $Id$

include $(top_srcdir)/Makefile.meta

ADT_TO_C = $(top_builddir)/scripts/adt-to-c-test
lib_LTLIBRARIES = libADT.la
nodist_include_HEADERS = ADT.h ADT_dict.h

nodist_libADT_la_SOURCES = ADT.c ADT_dict.c
libADT_la_CPPFLAGS = $(ATERM_CFLAGS) $(PT_SUPPORT_CFLAGS)
libADT_la_LDFLAGS = -avoid-version -no-undefined $(AM_LDFLAGS)
libADT_la_LIBADD = $(ATERM_LIBS) $(PT_SUPPORT_LIBS)

CLEANFILES += ADT.[ch] ADT.dict ADT_dict.[ch]
EXTRA_DIST += ADT.adt 

%INTERMEDIATE: ADT.c ADT_dict.c ADT.dict

ADT.c ADT.h ADT.dict ADT_dict.c ADT_dict.h: ADT.adt 
	$(ADT_TO_C) \
		--input $(top_srcdir)/lib/ADT.adt \
		--name ADT \
		--prefix ADT_
