# $Id$

include ../make_rules

# The following libraries are compiled to static libraries.
#
lib_LIBRARIES		= libATerm.a @ATERM_LIB_GCC@ @ATERM_LIB_CC@ \
			@ATERM_LIB_DBG@ @ATERM_LIB_PROF@ @ATERM_LIB_SHARED@
EXTRA_LIBRARIES		= libATerm-gcc.a libATerm-cc.a libATerm-dbg.a \
			libATerm-prof.a

ALLSRCS			= aterm.c list.c make.c memory.c afun.c gc.c \
			hash.c bafio.c byteio.c tafio.c version.c md5c.c

PUBLIC_INCL		= abool.h atypes.h aterm1.h aterm2.h deprecated.h \
			encoding.h afun.h

PRIVATE_INCL		= _aterm.h _afun.h debug.h gc.h list.h make.h \
			memory.h util.h bafio.h version.h tafio.h byteio.h \
			md5.h

CFLAGS			= -Wall -g -O2

ALLINCLUDES		= ${PUBLIC_INCL} ${PRIVATE_INCL}

include_HEADERS		= $(PUBLIC_INCL)

EXTRA_DIST		= $(ALLSRCS) $(PRIVATE_INCL)

# static only libraries
libATerm_a_SOURCES	=
libATerm_cc_a_SOURCES	=
libATerm_gcc_a_SOURCES	=
libATerm_dbg_a_SOURCES	=
libATerm_prof_a_SOURCES	=

libATerm_a_LIBADD	= $(ALLSRCS:.c=.o)
libATerm_cc_a_LIBADD	= $(ALLSRCS:.c=-cc.o)
libATerm_gcc_a_LIBADD	= $(ALLSRCS:.c=-gcc.o)
libATerm_dbg_a_LIBADD	= $(ALLSRCS:.c=-dbg.o)
libATerm_prof_a_LIBADD	= $(ALLSRCS:.c=-prof.o)

version.o:      version.c $(ALLINCLUDES) $(ALLSRCS)
version-cc.o:   version.c $(ALLINCLUDES) $(ALLSRCS)
version-gcc.o:  version.c $(ALLINCLUDES) $(ALLSRCS)
version-dbg.o:  version.c $(ALLINCLUDES) $(ALLSRCS)
version-prof.o: version.c $(ALLINCLUDES) $(ALLSRCS)

# vim:ts=8:sw=8:sts=0
