# $Id$

include ../make_rules

TESTS = stress

EXTRA_DIST = \
	error.trm \
	stress.dict \
	termstats.sh \
	test.trms \
	vararg1.c \
	vararg2.c \
	vararg3.c

MAINTAINERCLEANFILES = stress_dict.c stress_dict.h

INCLUDES = \
	-I$(top_srcdir) \
	-I$(top_srcdir)/aterm \
	-DSRCDIR="\"$(top_srcdir)/test/\""

LDFLAGS = -L$(top_builddir)/aterm
LDLIBS = -lATerm

noinst_PROGRAMS = stress primes randgen termstats fib

stress_SOURCES = stress_dict.c stress_dict.h stress.c
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

%_dict.c %_dict.h: %.dict ${top_builddir}/utils/dicttoc
	${top_builddir}/utils/dicttoc -dict $<
