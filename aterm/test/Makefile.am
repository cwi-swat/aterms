# $Id$

include $(top_srcdir)/Makefile.meta

TESTS = stress primes randgen fib
check_PROGRAMS = stress primes randgen termstats fib

EXTRA_DIST += \
  error.trm \
  terms.dict \
  termstats.sh \
  test.trms \
  vararg1.c \
  vararg2.c \
  vararg3.c

CLEANFILES += test.taf test.blob terms.c terms.h
BUILT_SOURCES = terms.c terms.h

AM_LDFLAGS  = -L$(top_builddir)/aterm 
LDADD       += -lATerm
AM_CPPFLAGS += \
  -I$(top_srcdir) \
  -I$(top_srcdir)/aterm \
  -DSRCDIR="\"$(top_srcdir)/test/\""

stress_SOURCES = stress.c
nodist_stress_SOURCES = terms.c terms.h

primes_SOURCES = primes.c
fib_SOURCES    = fib.c
randgen_SOURCES = randgen.c
termstats_SOURCES = termstats.c

# To be used to find out which vararg-implementation is used on a new
# platform. Do not remove, but uncomment as needed.

#vararg1_SOURCES = vararg1.c
#vararg2_SOURCES = vararg2.c
#vararg3_SOURCES = vararg3.c


.dict.c:
	${top_builddir}/utils/dicttoc -file-prefix $* -dict $<
