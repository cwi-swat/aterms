# $Id$
#
# vim:ts=8:sw=8
include ../make_rules

PROGS              = stress-cc stress-gcc stress-ns \
                     primes-cc primes-gcc  primes-ns \
                     asfixtest randgen \
                     vararg1 vararg2 vararg3 test-tool test-tool2

TESTS	           = $(PROGS) termstats.sh

noinst_PROGRAMS    = $(PROGS) termstats
stress_cc_SOURCES  = 
stress_cc_LDADD    = stress-cc.o $(CCLIBS)
stress_gcc_SOURCES = 
stress_gcc_LDADD   = stress-gcc.o $(GCCLIBS)
stress_ns_SOURCES  = 
stress_ns_LDADD    = stress-ns.o $(NSLIBS)

primes_cc_SOURCES  =
primes_cc_LDADD    = primes-cc.o $(CCLIBS)
primes_gcc_SOURCES =
primes_gcc_LDADD   = primes-gcc.o $(GCCLIBS)
primes_ns_SOURCES  =
primes_ns_LDADD    = primes-ns.o $(GCCLIBS)

vararg1_SOURCES    =
vararg1_LDADD      = vararg1-dbg.o $(DBGLIBS)

vararg2_SOURCES    =
vararg2_LDADD      = vararg3-dbg.o $(DBGLIBS)

vararg3_SOURCES    =
vararg3_LDADD      = vararg3-dbg.o $(DBGLIBS)

test_tool_SOURCES  =
test_tool_LDADD    = test-tool-dbg.o $(DBGLIBS)

test_tool2_SOURCES =
test_tool2_LDADD   = tool2.tif-dbg.o test-tool2-dbg.o $(DBGLIBS)

asfixtest_SOURCES  = asfixtest.c
asfixtest_LDADD    = $(CCLIBS)

randgen_SOURCES    = randgen.c
randgen_LDADD      = $(CCLIBS)

termstats_SOURCES  = termstats.c
termstats_LDADD    = $(CCLIBS)

tool2.tif-dbg.o : tool2.tif.c

tool2.tif-dbg.o : tool2.tif.c tool2.tif.h

tool2.tif.c tool2.tif.h : test2.tifs
	$(top_builddir)/toolbus/tifstoc -tool tool2 $<

LDFLAGS = -L$(top_builddir)/aterm/.libs \
          -L$(top_builddir)/asfix/.libs \
          -L$(top_builddir)/toolbus/.libs

CCLIBS  = ${top_builddir}/asfix/libAsFix-cc.a \
          ${top_builddir}/toolbus/libATB-cc.a \
          ${top_builddir}/aterm/libATerm-cc.a

GCCLIBS = ${top_builddir}/asfix/libAsFix-gcc.la \
          ${top_builddir}/toolbus/libATB-gcc.la \
          ${top_builddir}/aterm/libATerm-gcc.la

DBGLIBS = ${top_builddir}/asfix/libAsFix-dbg.la \
          ${top_builddir}/toolbus/libATB-dbg.la \
          ${top_builddir}/aterm/libATerm-dbg.la

NSLIBS  = ${top_builddir}/asfix/libAsFix-ns.a \
          ${top_builddir}/toolbus/libATB-ns.a \
          ${top_builddir}/aterm/libATerm-ns.a

EXTRA_DIST = stress.c asfixtest.c primes.c test-tool.c test-tool2.c \
             randgen.c termstats.c termstats.sh vararg1.c vararg2.c vararg3.c \
             test.tb test2.tb Test.asfix test.trms error.trm 

INCLUDES   = -I. -I$(top_srcdir)/asfix -I$(top_srcdir)/aterm -I$(top_srcdir)/toolbus

termstats.sh : termstats

# The output of $(TOOLBUS) is moved to $@ to make sure the generated .tifs
# file exists in the current directory. The ToolBus by default uses the
# dirname of its second argument to determine the location where to store
# its output. This does not work correct when using VPATH in the Makefile.
test2.tifs: test2.tb
	$(TOOLBUS) -gentifs $<
	@if [ "a${VPATH}" != "a" ]; then \
	   mv ${srcdir}/test2.tifs $@ ;\
	fi
