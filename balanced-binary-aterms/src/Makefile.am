
lib_LIBRARIES = libbbtree.a

LDLIBS = -lATerm 

include_HEADERS = \
        bbtree.h

libbbtree_a_SOURCES = \
        bbtree.c

AM_CFLAGS = -Wall -pedantic -g -O2

AM_LDFLAGS = -L${ATERM}/lib -L .

INCLUDES = \
        -I${ATERM}/include 
