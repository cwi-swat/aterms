include $(top_srcdir)/Makefile.meta

lib_LTLIBRARIES = librelational-aterms.la

include_HEADERS = \
	relational-aterms.h

librelational_aterms_la_CPPFLAGS = $(ATERM_CFLAGS) $(BALANCED_BINARY_ATERMS_LIBS)
librelational_aterms_la_LDFLAGS  = -avoid-version -no-undefined $(AM_LDFLAGS)
librelational_aterms_la_LIBADD = $(ATERM_LIBS) $(BALANCED_BINARY_ATERMS_LIBS)
librelational_aterms_a_SOURCES = \
	relational-aterms.c
