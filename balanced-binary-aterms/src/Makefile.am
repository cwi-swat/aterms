
lib_LTLIBRARIES = libbbtree.la

include_HEADERS = \
        bbtree.h

libbbtree_la_CPPFLAGS = $(ATERM_CFLAGS)
libbbtree_la_LDFLAGS  = -avoid-version -no-undefined $(AM_LDFLAGS)
libbbtree_la_LIBADD = $(ATERM_LIBS)
libbbtree_a_SOURCES = \
        bbtree.c
