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

bin_PROGRAMS		= tifstoc tblog

LDFLAGS			= -L${top_builddir}/aterm/ -L.
LDLIBS			= -lATB -lATerm

tifstoc_SOURCES		= tifstoc.c atb-tool.h
tifstoc_LDADD		= libATB.a -L${top_builddir}/aterm/ -lATerm

tblog_SOURCES		= tblog.c atb-tool.h
tblog_LDADD		= tblog_dict.o logger.tif.o ${LDLIBS}

INCLUDES		= -I$(top_srcdir)/aterm

# The following libraries are compiled to static libraries.
lib_LIBRARIES		= libATB.a @TOOLBUS_LIB_GCC@ @TOOLBUS_LIB_CC@ @TOOLBUS_LIB_DBG@ \
			 @TOOLBUS_LIB_PROF@ @TOOLBUS_LIB_SHARED@
EXTRA_LIBRARIES		= libATB-gcc.a libATB-cc.a libATB-dbg.a libATB-prof.a

ALLSRCS			= atb-tool.c

PUBLIC_INCL		= atb-tool.h

PRIVATE_INCL		=

ALL_INCLUDES		= ${PUBLIC_INCL} ${PRIVATE_INCL}

include_HEADERS		= $(PUBLIC_INCL)

EXTRA_DIST		= $(ALLSRCS) tblog-interface.tb tblog.dict

CLEANFILES		= tblog-interface.tifs logger.tif.c logger.tif.h \
			tblog_dict.c tblog_dict.h

# static only libraries
libATB_a_SOURCES	=
libATB_cc_a_SOURCES	=
libATB_gcc_a_SOURCES	=
libATB_dbg_a_SOURCES	=
libATB_prof_a_SOURCES	=

libATB_a_LIBADD		= $(ALLSRCS:.c=.o)
libATB_cc_a_LIBADD	= $(ALLSRCS:.c=-cc.o)
libATB_gcc_a_LIBADD	= $(ALLSRCS:.c=-gcc.o)
libATB_dbg_a_LIBADD	= $(ALLSRCS:.c=-dbg.o)
libATB_prof_a_LIBADD	= $(ALLSRCS:.c=-prof.o)

logger.tif.c logger.tif.h : tblog-interface.tifs
	PATH=$(top_builddir)/toolbus/:$$PATH tifstoc -tool logger $<

logger.tif.o: logger.tif.c logger.tif.h

tblog_dict.o: tblog_dict.c tblog_dict.h
tblog.o: tblog_dict.h logger.tif.h

%_dict.c %_dict.h: %.dict ${top_builddir}/utils/dicttoc
	${top_builddir}/utils/dicttoc -dict $<

# The output of $(TOOLBUS) is moved to $@ to make sure the generated .tifs
# file exists in the current directory. The ToolBus by default uses the
# dirname of its second argument to determine the location where to store
# its output. This does not work correct when using VPATH in the Makefile.
tblog-interface.tifs: tblog-interface.tb
	$(TOOLBUS) -gentifs $<
	@if [ "a${VPATH}" != "a" ]; then \
	  mv ${srcdir}/tblog-interface.tifs $@ ;\
	fi
