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

INCLUDES		= -I$(top_srcdir)/aterm

LDFLAGS			= -L$(top_builddir)/aterm/

LDLIBS			= -lATerm

bin_PROGRAMS		= termsize baffle dicttoc atsum

termsize_SOURCES	= termsize.c
termsize_LDADD		= $(LDLIBS)

baffle_SOURCES		= baffle.c
baffle_LDADD		= $(LDLIBS)

dicttoc_SOURCES		= dicttoc.c
dicttoc_LDADD		= $(LDLIBS)

atsum_SOURCES		= atsum.c
atsum_LDADD		= $(LDLIBS)

bin_SCRIPTS		= baf2trm baf2taf trm2baf trm2taf taf2trm taf2baf \
			  trmcat

CFLAGS			= -Wall -g -O2


EXTRA_DIST		= $(bin_SCRIPTS)
