# $Id$
#
# vim:ts=8:sw=8
include ../make_rules

bin_PROGRAMS           = tifstoc

tifstoc_SOURCES        = tifstoc.c atb-tool.h
tifstoc_LDADD          = libATB-cc.a \
                         -L ${top_builddir}/aterm/ -lATerm-cc
tifstoc_LDFLAGS        = -static

INCLUDES               = -I$(top_srcdir)/aterm

# The following libraries are compiled to static libraries.
#
# Because of the inability of libtool (version 1.3.4 and before) to be used
# in combination with different compilers simultaneously, we can only
# generate shared libraries using gcc. This means that for some targets we
# can only generate static libraries.
lib_LIBRARIES          = libATB-cc.a libATB-ns.a

# These libraries are compiled as static and shared libraries
lib_LTLIBRARIES        = libATB-gcc.la libATB-dbg.la libATB-prof.la

ALLSRCS                = atb-tool.c

PUBLIC_INCL            = atb-tool.h

PRIVATE_INCL           =

ALL_INCLUDES           = ${PUBLIC_INCL} ${PRIVATE_INCL}

include_HEADERS        = $(PUBLIC_INCL)

EXTRA_DIST             = $(ALLSRCS)


# static only libraries
libATB_cc_a_SOURCES    =
libATB_ns_a_SOURCES    =

libATB_cc_a_LIBADD     = $(ALLSRCS:.c=-cc.o)
libATB_ns_a_LIBADD     = $(ALLSRCS:.c=-ns.o)

# static/shared libraries
libATB_gcc_la_SOURCES  =
libATB_dbg_la_SOURCES  =
libATB_prof_la_SOURCES =
                                         
libATB_gcc_la_LIBADD   = $(ALLSRCS:.c=-gcc.lo)
libATB_dbg_la_LIBADD   = $(ALLSRCS:.c=-dbg.lo)
libATB_prof_la_LIBADD  = $(ALLSRCS:.c=-prof.lo)
