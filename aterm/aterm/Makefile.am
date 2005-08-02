lib_LTLIBRARIES = libATerm.la libATerm-dbg.la libATerm-prof.la
include_HEADERS = $(PUBLIC_INCL)
noinst_HEADERS  = $(PRIVATE_INCL)

AM_CPPFLAGS = -I$(srcdir) 

libATerm_la_SOURCES      = $(ALLSRCS)
libATerm_dbg_la_SOURCES  = $(ALLSRCS)
libATerm_prof_la_SOURCES = $(ALLSRCS)

libATerm_la_CFLAGS      = $(OPTIMIZECFLAGS) $(WARNINGCFLAGS) -DNDEBUG
libATerm_dbg_la_CFLAGS  = $(WARNINGCFLAGS) -g
libATerm_prof_la_CFLAGS = $(WARNINGCFLAGS) -pg

# prof library uses external symbols (-no-undefined is not valid)

libATerm_la_LDFLAGS      = -avoid-version -no-undefined
libATerm_dbg_la_LDFLAGS  = -avoid-version -no-undefined
libATerm_prof_la_LDFLAGS = -avoid-version

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
