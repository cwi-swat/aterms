# $Id$
#
# vim:ts=8:sw=8
include ../make_rules

bin_PROGRAMS           = tifstoc

tifstoc_SOURCES        = tifstoc.c atb-tool.h
tifstoc_LDADD          = libATB-cc.a \
                         -L${top_builddir}/aterm/ -lATerm-cc

INCLUDES               = -I$(top_srcdir)/aterm

# The following libraries are compiled to static libraries.
lib_LIBRARIES          = libATB-cc.a libATB-ns.a \
                         libATB-gcc.a libATB-dbg.a libATB-prof.a

ALLSRCS                = atb-tool.c

PUBLIC_INCL            = atb-tool.h

PRIVATE_INCL           =

ALL_INCLUDES           = ${PUBLIC_INCL} ${PRIVATE_INCL}

include_HEADERS        = $(PUBLIC_INCL)

EXTRA_DIST             = $(ALLSRCS)


# static only libraries
libATB_cc_a_SOURCES    =
libATB_ns_a_SOURCES    =
libATB_gcc_a_SOURCES   =
libATB_dbg_a_SOURCES   =
libATB_prof_a_SOURCES  =

libATB_cc_a_LIBADD     = $(ALLSRCS:.c=-cc.o)
libATB_ns_a_LIBADD     = $(ALLSRCS:.c=-ns.o)
libATB_gcc_a_LIBADD    = $(ALLSRCS:.c=-gcc.o)
libATB_dbg_a_LIBADD    = $(ALLSRCS:.c=-dbg.o)
libATB_prof_a_LIBADD   = $(ALLSRCS:.c=-prof.o)
