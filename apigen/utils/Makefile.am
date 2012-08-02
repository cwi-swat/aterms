# $Id$

include $(top_srcdir)/Makefile.meta

bin_PROGRAMS = concat-adt

concat_adt_SOURCES = concat-adt.c
concat_adt_LDADD = ../lib/libADT.la $(ATERM_LIBS) 
concat_adt_CPPFLAGS = -I$(top_srcdir)/lib $(ATERM_CFLAGS)
