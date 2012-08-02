lib_LTLIBRARIES = libATerm.la 
include_HEADERS = $(PUBLIC_INCL)
noinst_HEADERS  = $(PRIVATE_INCL)

AM_CPPFLAGS = -I$(srcdir) 

libATerm_la_SOURCES = $(ALLSRCS)
libATerm_la_CFLAGS = -DNDEBUG
libATerm_la_LDFLAGS = -avoid-version -no-undefined

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
  memmgnt.c \
  tafio.c \
  version.c \
  safio.c \
  idmappings.c \
  byteencoding.c

PUBLIC_INCL = \
  abool.h \
  afun.h \
  aterm1.h \
  aterm2.h \
  atypes.h \
  deprecated.h \
  encoding.h \
  safio.h \
  idmappings.h

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
  version.h \
  byteencoding.h
