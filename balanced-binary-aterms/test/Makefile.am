
include $(top_srcdir)/Makefile.meta

TESTS = bbtree_test

check_PROGRAMS = bbtree_test

bbtree_test_SOURCES = bbtree_test.c
bbtree_test_CPPFLAGS = \
        -I$(top_srcdir)/src \
	$(BALANCED_BINARY_ATERMS_CFLAGS) \
	$(ATERM_CFLAGS) 

bbtree_test_LDADD = \
	     $(BALANCED_BINARY_ATERMS_LIBS) \
	     -L$(top_builddir)/src -lbbtree -lATerm

