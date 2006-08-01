
include $(top_srcdir)/Makefile.meta

TESTS = test pico

EXTRA_DIST = fac.ast

check_PROGRAMS = test pico

test_SOURCES = test.c
test_CPPFLAGS = \
        -I$(top_srcdir)/src \
	$(BALANCED_BINARY_ATERMS_CFLAGS) 
test_LDADD = \
	     $(BALANCED_BINARY_ATERMS_LIBS) \
	     -L$(top_builddir)/src -lrelational-aterms

pico_SOURCES = pico.c
pico_CPPFLAGS = -DSRCDIR="\"${top_srcdir}/test\"" \
        -I$(top_srcdir)/src \
	$(BALANCED_BINARY_ATERMS_CFLAGS) 
pico_LDADD = \
	     $(BALANCED_BINARY_ATERMS_LIBS) \
	     -L$(top_builddir)/src -lrelational-aterms

