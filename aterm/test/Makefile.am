#
#    ATerm -- The ATerm (Annotated Term) library
#    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
#                             The  Netherlands.
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
#
# $Id$
#
# vim:ts=8:sw=8
include ../make_rules

PROGS              	= stress primes randgen termstats \
			  vararg1 vararg2 vararg3 test-tool test-tool2

TESTS	          	= stress

EXTRA_DIST = termstats.sh test.tb test2.tb test.trms error.trm  \
             tool2.tif.c tool2.tif.h stress.dict

CLEANFILES = test2.tifs tool2.tif.c tool2.tif.h stress_dict.c stress_dict.h

INCLUDES   = -I. -I$(top_srcdir)/aterm -I$(top_srcdir)/toolbus

CFLAGS                  = -Wall -g

LDFLAGS 		= -L$(top_builddir)/aterm/ \
			  -L$(top_builddir)/toolbus/

LDLIBS			= -lATB -lATerm

noinst_PROGRAMS		= $(PROGS)
stress_SOURCES		= stress.c 
stress_LDADD		= stress_dict.o $(LDLIBS)

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

randgen_SOURCES    	= randgen.c
randgen_LDADD      	= $(LDLIBS)

termstats_SOURCES  	= termstats.c
termstats_LDADD    	= $(LDLIBS)

test-tool2.o : tool2.tif.h

tool2.tif.o : tool2.tif.c tool2.tif.h

%.tif.c %.tif.h : test2.tifs
	PATH=$(top_builddir)/toolbus/:$$PATH tifstoc -tool tool2 $<

stress_dict.o: stress_dict.c stress_dict.h
stress.o: stress_dict.h

%_dict.c %_dict.h: %.dict ${top_srcdir}/utils/dicttoc
	${top_srcdir}/utils/dicttoc -dict $<


test: stress
	./stress

# The output of $(TOOLBUS) is moved to $@ to make sure the generated .tifs
# file exists in the current directory. The ToolBus by default uses the
# dirname of its second argument to determine the location where to store
# its output. This does not work correct when using VPATH in the Makefile.
%.tifs: %.tb
	$(TOOLBUS) -gentifs $<
	@if [ "a${VPATH}" != "a" ]; then \
	   mv ${srcdir}/test2.tifs $@ ;\
	fi
