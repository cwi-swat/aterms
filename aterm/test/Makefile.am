# $Id$

include ../make_rules

TESTS = stress

EXTRA_DIST = \
	error.trm \
	terms.dict \
	termstats.sh \
	test.trms \
	vararg1.c \
	vararg2.c \
	vararg3.c

CLEANFILES = test.taf test.blob terms.c terms.h

BUILTSOURCES = terms.c terms.h

INCLUDES = \
	-I$(top_srcdir) \
	-I$(top_srcdir)/aterm \
	-DSRCDIR="\"$(top_srcdir)/test/\""

AM_LDFLAGS = -L$(top_builddir)/aterm
LDLIBS = -lATerm

noinst_PROGRAMS = stress primes randgen termstats fib

stress_SOURCES = terms.c terms.h stress.c
stress_LDADD = $(LDLIBS)

primes_SOURCES = primes.c
primes_LDADD = $(LDLIBS)

fib_SOURCES = fib.c
fib_LDADD = $(LDLIBS)

# To be used to find out which vararg-implementation is used on a new
# platform. Do not remove, but uncomment as needed.

#vararg1_SOURCES = vararg1.c
#vararg1_LDADD = $(LDLIBS)
#vararg2_SOURCES = vararg2.c
#vararg2_LDADD = $(LDLIBS)
#vararg3_SOURCES = vararg3.c
#vararg3_LDADD = $(LDLIBS)

randgen_SOURCES = randgen.c
randgen_LDADD = $(LDLIBS)

termstats_SOURCES = termstats.c
termstats_LDADD = $(LDLIBS)

.dict.c:
	${top_builddir}/utils/dicttoc -file-prefix $* -dict $<
