
AM_CFLAGS = -Wall -pedantic -g -O2

TESTS = test

INCLUDES = \
        -I$(top_srcdir)/src \
	-I$(ATERM)/include \
	-I$(BALANCED_BINARY_TREES)/include

AM_LDFLAGS = \
	-L$(top_builddir)/src \
	-L$(BALANCED_BINARY_TREES)/lib \
	-L$(ATERM)/lib

LDLIBS = -lATerm-dbg -lATerm-relations -lbbtree

noinst_PROGRAMS = test

test_SOURCES = test.c assert-equals.c
test_LDADD = $(LDLIBS)

