# $Id$
#
# vim:ts=8:sw=8
include ../make_rules

PROGS              	= stress primes asfixtest randgen termstats \
			  vararg1 vararg2 vararg3 test-tool test-tool2

TESTS	          	= $(PROGS) termstats.sh

INCLUDES   = -I. -I$(top_srcdir)/asfix -I$(top_srcdir)/aterm -I$(top_srcdir)/toolbus

LDFLAGS 		= -L$(top_builddir)/aterm/ \
			  -L$(top_builddir)/asfix/ \
			  -L$(top_builddir)/toolbus/

LDLIBS			= -lAsFix -lATB -lATerm

noinst_PROGRAMS		= $(PROGS)
stress_SOURCES		= stress.c 
stress_LDADD		= $(LDLIBS)

primes_SOURCES		= primes.c
primes_LDADD		= $(LDLIBS)

vararg1_SOURCES    	= vararg1.c
vararg1_LDADD      	= $(LDLIBS)

vararg2_SOURCES    	= vararg2.c
vararg2_LDADD      	= $(LDLIBS)

vararg3_SOURCES    	= vararg3.c
vararg3_LDADD      	= $(LDLIBS)

test_tool_SOURCES  	= test-tool.c
test_tool_LDADD    	= $(LDLIBS)

test_tool2_SOURCES	= test-tool2.c
test_tool2_LDADD	= tool2.tif.o $(LDLIBS)

asfixtest_SOURCES  	= asfixtest.c
asfixtest_LDADD    	= $(LDLIBS)

randgen_SOURCES    	= randgen.c
randgen_LDADD      	= $(LDLIBS)

termstats_SOURCES  	= termstats.c
termstats_LDADD    	= $(LDLIBS)

test-tool2.c : tool2.tif.h

tool2.tif.o : tool2.tif.c tool2.tif.h

tool2.tif.c tool2.tif.h : test2.tifs
	$(top_builddir)/toolbus/tifstoc -tool tool2 $<

EXTRA_DIST = termstats.sh test.tb test2.tb Test.asfix test.trms error.trm 

termstats.sh : termstats

test: stress
	./stress

# The output of $(TOOLBUS) is moved to $@ to make sure the generated .tifs
# file exists in the current directory. The ToolBus by default uses the
# dirname of its second argument to determine the location where to store
# its output. This does not work correct when using VPATH in the Makefile.
test2.tifs: test2.tb
	$(TOOLBUS) -gentifs $<
	@if [ "a${VPATH}" != "a" ]; then \
	   mv ${srcdir}/test2.tifs $@ ;\
	fi
