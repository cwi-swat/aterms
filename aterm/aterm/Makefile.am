# $Id$
#
# vim:ts=8:sw=8

include ../make_rules

bin_PROGRAMS             = termsize baffle

termsize_SOURCES         = termsize.c  $(ALLINCLUDES)
termsize_LDADD           = libATerm-cc.a

baffle_SOURCES           = baffle.c  $(ALLINCLUDES)
baffle_LDADD             = libATerm-cc.a

bin_SCRIPTS              = baf2trm trm2baf trmcat

# The following libraries are compiled to static libraries.
#
lib_LIBRARIES            = libATerm-cc.a libATerm-ns.a @LIB_CC64@ \
                           libATerm-gcc.a \
                           libATerm-dbg.a \
                           libATerm-prof.a

EXTRA_LIBRARIES          = libATerm-cc64.a 

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
libATerm_gcc_a_SOURCES   =
libATerm_dbg_a_SOURCES   =
libATerm_prof_a_SOURCES  =

libATerm_cc_a_LIBADD     = $(ALLSRCS:.c=-cc.o)
libATerm_ns_a_LIBADD     = $(ALLSRCS:.c=-ns.o)
libATerm_cc64_a_LIBADD   = $(ALLSRCS:.c=-cc64.o)
libATerm_gcc_a_LIBADD    = $(ALLSRCS:.c=-gcc.o)
libATerm_dbg_a_LIBADD    = $(ALLSRCS:.c=-dbg.o)
libATerm_prof_a_LIBADD   = $(ALLSRCS:.c=-prof.o)

version-cc.o:   version.c $(ALLINCLUDES) $(ALLSRCS)
version-cc64.o: version.c $(ALLINCLUDES) $(ALLSRCS)
version-gcc.o:  version.c $(ALLINCLUDES) $(ALLSRCS)
version-dbg.o:  version.c $(ALLINCLUDES) $(ALLSRCS)
version-prof.o: version.c $(ALLINCLUDES) $(ALLSRCS)
version-ns.o:   version.c $(ALLINCLUDES) $(ALLSRCS)

