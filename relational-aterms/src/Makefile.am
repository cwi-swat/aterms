
lib_LIBRARIES = librelational-aterms.a

include_HEADERS = \
	relational-aterms.h

librelational_aterms_a_SOURCES = \
	relational-aterms.c

AM_CFLAGS = -Wall -pedantic -g -O2

AM_LDFLAGS = \
	-L${ATERM}/lib \
	-L${BALANCED_BINARY_ATERMS}/lib

INCLUDES = \
        -I${ATERM}/include \
        -I${BALANCED_BINARY_ATERMS}/include 

