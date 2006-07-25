
include $(top_srcdir)/Makefile.meta

TESTS = test pico

EXTRA_DIST = fac.ast

INCLUDES = \
        -I$(top_srcdir)/src \
	$(ATERM_CFLAGS) \
	$(BALANCED_BINARY_ATERMS_CFLAGS) 

AM_LDFLAGS = \
	$(ATERM_LIBS) \
	$(BALANCED_BINARY_ATERMS_LIBS) \
	-L$(top_builddir)/src -lrelational-aterms

noinst_PROGRAMS = test pico

test_SOURCES = test.c
test_LDADD = $(AM_LDFLAGS)

pico_SOURCES = pico.c
pico_LDADD = $(AM_LDFLAGS)

