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

bin_PROGRAMS           = tifstoc

tifstoc_SOURCES        = tifstoc.c atb-tool.h
tifstoc_LDADD          = libATB.a -L${top_builddir}/aterm/ -lATerm

INCLUDES               = -I$(top_srcdir)/aterm

# The following libraries are compiled to static libraries.
lib_LIBRARIES          = libATB.a @TOOLBUS_LIB_GCC@ @TOOLBUS_LIB_CC@ @TOOLBUS_LIB_DBG@ \
			 @TOOLBUS_LIB_PROF@ @TOOLBUS_LIB_CC64@ @TOOLBUS_LIB_SHARED@
EXTRA_LIBRARIES	       = libATB-gcc.a libATB-cc.a libATB-dbg.a libATB-prof.a libATB-cc64.a

ALLSRCS                = atb-tool.c

PUBLIC_INCL            = atb-tool.h

PRIVATE_INCL           =

ALL_INCLUDES           = ${PUBLIC_INCL} ${PRIVATE_INCL}

include_HEADERS        = $(PUBLIC_INCL)

EXTRA_DIST             = $(ALLSRCS)


# static only libraries
libATB_a_SOURCES       =
libATB_cc_a_SOURCES    =
libATB_cc64_a_SOURCES  =
libATB_gcc_a_SOURCES   =
libATB_dbg_a_SOURCES   =
libATB_prof_a_SOURCES  =

libATB_a_LIBADD		= $(ALLSRCS:.c=.o)
libATB_cc_a_LIBADD     	= $(ALLSRCS:.c=-cc.o)
libATB_cc64_a_LIBADD    = $(ALLSRCS:.c=-ns.o)
libATB_gcc_a_LIBADD     = $(ALLSRCS:.c=-gcc.o)
libATB_dbg_a_LIBADD     = $(ALLSRCS:.c=-dbg.o)
libATB_prof_a_LIBADD   	= $(ALLSRCS:.c=-prof.o)
