include $(top_srcdir)/Makefile.meta

lib_LIBRARIES = librelational-aterms.a

include_HEADERS = \
	relational-aterms.h

librelational_aterms_a_SOURCES = \
	relational-aterms.c

AM_LDFLAGS = \
	$(ATERM_LIBS) \
	$(BALANCED_BINARY_ATERMS_LIBS) 

INCLUDES = \
        ${ATERM_CFLAGS} \
        ${BALANCED_BINARY_ATERMS_CFLAGS} 

