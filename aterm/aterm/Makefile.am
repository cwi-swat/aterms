# $Id$
#
# vim:ts=8:sw=8

include ../make_rules

bin_PROGRAMS             = termsize baffle

termsize_SOURCES         = termsize.c  $(ALLINCLUDES)
termsize_LDADD           = libATerm-cc.a
termsize_LDFLAGS         = -static

baffle_SOURCES           = baffle.c  $(ALLINCLUDES)
baffle_LDADD             = libATerm-cc.a
baffle_LDFLAGS           = -static

bin_SCRIPTS              = baf2trm trm2baf trmcat

# The following libraries are compiled to static libraries.
#
# Because of the inability of libtool (version 1.3.4 and before) to be used
# in combination with different compilers simultaneously, we can only
# generate shared libraries using gcc. This means that for some targets we
# can only generate static libraries.
lib_LIBRARIES            = libATerm-cc.a libATerm-ns.a @LIB_CC64@

EXTRA_LIBRARIES          = libATerm-cc64.a

# These libraries are compiled as static and shared libraries
lib_LTLIBRARIES          = libATerm-gcc.la \
                           libATerm-dbg.la \
                           libATerm-prof.la 

ALLSRCS                  = aterm.c list.c make.c memory.c afun.c gc.c \
                           hash.c bafio.c version.c

PUBLIC_INCL              = abool.h aterm1.h aterm2.h deprecated.h \
                           encoding.h afun.h

PRIVATE_INCL             = _aterm.h debug.h gc.h list.h make.h memory.h \
                           util.h bafio.h version.h

ALLINCLUDES              = ${PUBLIC_INCL} ${PRIVATE_INCL}

include_HEADERS          = $(PUBLIC_INCL)

EXTRA_DIST               = $(ALLSRCS) $(bin_SCRIPTS)

# static only libraries
libATerm_cc_a_SOURCES    =
libATerm_cc64_a_SOURCES  =
libATerm_ns_a_SOURCES    =

libATerm_cc_a_LIBADD     = $(ALLSRCS:.c=-cc.o)
libATerm_ns_a_LIBADD     = $(ALLSRCS:.c=-ns.o)
libATerm_cc64_a_LIBADD   = $(ALLSRCS:.c=-cc64.o)

# static/shared libraries
libATerm_gcc_la_SOURCES  =
libATerm_dbg_la_SOURCES  =
libATerm_prof_la_SOURCES =

libATerm_gcc_la_LIBADD   = $(ALLSRCS:.c=-gcc.lo)
libATerm_dbg_la_LIBADD   = $(ALLSRCS:.c=-dbg.lo)
libATerm_prof_la_LIBADD  = $(ALLSRCS:.c=-prof.lo)

version-cc.o:   version.c $(ALLINCLUDES) $(ALLSRCS)
version-cc64.o: version.c $(ALLINCLUDES) $(ALLSRCS)
version-gcc.o:  version.c $(ALLINCLUDES) $(ALLSRCS)
version-dbg.o:  version.c $(ALLINCLUDES) $(ALLSRCS)
version-prof.o: version.c $(ALLINCLUDES) $(ALLSRCS)
version-ns.o:   version.c $(ALLINCLUDES) $(ALLSRCS)

