# $Id$

include ../make_rules


TESTS	          	= stress

EXTRA_DIST 		= termstats.sh test.trms error.trm stress.dict
CLEANFILES		= stress_dict.c stress_dict.h

INCLUDES		= -I$(top_srcdir)/aterm
CFLAGS                  = -Wall -Werror -g -DSRCDIR="\"$(top_srcdir)/test/\""
LDFLAGS 		= -L$(top_builddir)/aterm/
LDLIBS			= -lATerm

noinst_PROGRAMS		= stress primes randgen termstats vararg1 vararg2 \
			vararg3

stress_SOURCES		= stress_dict.c stress_dict.h stress.c
stress_LDADD		= $(LDLIBS)

primes_SOURCES		= primes.c
primes_LDADD		= $(LDLIBS)

vararg1_SOURCES    	= vararg1.c
vararg1_LDADD      	= $(LDLIBS)

vararg2_SOURCES    	= vararg2.c
vararg2_LDADD      	= $(LDLIBS)

vararg3_SOURCES    	= vararg3.c
vararg3_LDADD      	= $(LDLIBS)

randgen_SOURCES    	= randgen.c
randgen_LDADD      	= $(LDLIBS)

termstats_SOURCES  	= termstats.c
termstats_LDADD    	= $(LDLIBS)

%_dict.c %_dict.h: %.dict ${top_builddir}/utils/dicttoc
	${top_builddir}/utils/dicttoc -dict $<
