
AM_CFLAGS = -Wall -pedantic -g -O2

TESTS = test

INCLUDES = \
        -I$(top_srcdir)/src \
	-I$(ATERM)/include \
	-I$(BALANCED_BINARY_ATERMS)/include

AM_LDFLAGS = \
	-L$(top_builddir)/src \
	-L$(BALANCED_BINARY_ATERMS)/lib \
	-L$(ATERM)/lib

LDLIBS = -lATerm-dbg -lrelational-aterms -lbbtree

noinst_PROGRAMS = test

test_SOURCES = test.c assert-equals.c
test_LDADD = $(LDLIBS)

