# $Id$

include ../make_rules

lib_LIBRARIES = \
	libATerm.a \
	@ATERM_LIB_CC64@
	@ATERM_LIB_CC@ \
	@ATERM_LIB_DBG@ \
	@ATERM_LIB_GCC@ \
	@ATERM_LIB_NS@ \
	@ATERM_LIB_PROF@

EXTRA_LIBRARIES = \
	libATerm-gcc.a \
	libATerm-cc.a \
	libATerm-dbg.a \
	libATerm-prof.a \
	libATerm-ns.a \
	libATerm-pem.a \
	libATerm-cc64.a

ALLSRCS = \
	afun.c \
	aterm.c \
	bafio.c \
	byteio.c \
	gc.c \
	hash.c \
	list.c \
	make.c \
	md5c.c \
	memory.c \
	tafio.c \
	version.c

PUBLIC_INCL = \
	abool.h \
	afun.h \
	aterm1.h \
	aterm2.h \
	atypes.h \
	deprecated.h \
	encoding.h

PRIVATE_INCL = \
	_afun.h \
	_aterm.h \
	bafio.h \
	byteio.h \
	debug.h \
	gc.h \
	list.h \
	make.h \
	md5.h \
	memory.h \
	tafio.h \
	util.h \
	version.h

ALLINCLUDES = ${PUBLIC_INCL} ${PRIVATE_INCL}

include_HEADERS = $(PUBLIC_INCL)

EXTRA_DIST = $(ALLSRCS) $(PRIVATE_INCL)

# static only libraries
libATerm_a_SOURCES	= $(ALLSRCS)
libATerm_cc_a_SOURCES	=
libATerm_gcc_a_SOURCES	=
libATerm_dbg_a_SOURCES	=
libATerm_prof_a_SOURCES	=
libATerm_ns_a_SOURCES	=
libATerm_pem_a_SOURCES	=
libATerm_cc64_a_SOURCES	=

libATerm_ns_a_CFLAGS	= -DNO_SHARING

libATerm_cc_a_LIBADD	= $(ALLSRCS:.c=-cc.o)
libATerm_gcc_a_LIBADD	= $(ALLSRCS:.c=-gcc.o)
libATerm_dbg_a_LIBADD	= $(ALLSRCS:.c=-dbg.o)
libATerm_prof_a_LIBADD	= $(ALLSRCS:.c=-prof.o)
libATerm_ns_a_LIBADD	= $(ALLSRCS:.c=-ns.o)
libATerm_pem_a_LIBADD	= $(ALLSRCS:.c=-pem.o)
libATerm_cc64_a_LIBADD	= $(ALLSRCS:.c=-cc64.o)

version.o:      version.c $(ALLINCLUDES) $(ALLSRCS)
version-cc.o:   version.c $(ALLINCLUDES) $(ALLSRCS)
version-gcc.o:  version.c $(ALLINCLUDES) $(ALLSRCS)
version-dbg.o:  version.c $(ALLINCLUDES) $(ALLSRCS)
version-prof.o: version.c $(ALLINCLUDES) $(ALLSRCS)
version-ns.o:   version.c $(ALLINCLUDES) $(ALLSRCS)
version-pem.o:  version.c $(ALLINCLUDES) $(ALLSRCS)
version-cc64.o: version.c $(ALLINCLUDES) $(ALLSRCS)
