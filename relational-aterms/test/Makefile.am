
AM_CFLAGS = -Wall -pedantic -g -O2

TESTS = test pico

INCLUDES = \
        -I$(top_srcdir)/src \
	-I$(ATERM)/include \
	-I$(BALANCED_BINARY_ATERMS)/include

AM_LDFLAGS = \
	-L$(ATERM)/lib \
	-L$(BALANCED_BINARY_ATERMS)/lib \
	-L$(top_builddir)/src

LDLIBS = -lATerm -lrelational-aterms -lbbtree 

noinst_PROGRAMS = test pico

test_SOURCES = test.c
test_LDADD = $(LDLIBS)

pico_SOURCES = pico.c
pico_LDADD = $(LDLIBS)

