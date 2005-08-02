# $Id$

EXTRA_DIST       = $(bin_SCRIPTS) 

AM_CPPFLAGS      = -I$(top_srcdir)/aterm
AM_LDFLAGS       = -L$(top_builddir)/aterm
LDADD            = -lATerm

bin_PROGRAMS     = termsize baffle dicttoc atsum atdiff atrmannos

termsize_SOURCES  = termsize.c
baffle_SOURCES    = baffle.c
dicttoc_SOURCES   = dicttoc.c
atsum_SOURCES     = atsum.c
atdiff_SOURCES    = atdiff.c
atrmannos_SOURCES = atrmannos.c

bin_SCRIPTS = \
  baf2trm baf2taf trm2baf trm2taf taf2trm taf2baf trmcat
