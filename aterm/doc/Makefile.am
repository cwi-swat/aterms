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
EXTRA_DIST = append.ltx at.ltx aterm.ltx corrections.ltx \
             corrections2.ltx gtld-at.ltx manual.ltx thesis.bib \
             encoding.fig header.fig hierarchy.fig sharing.eps \
             speauth.cls spedoc.tex \
             general-make at.ps manual.ps

SUBDIRS=examples spec

include ${srcdir}/general-make

CHAPS	     = at.ltx
PICS	     = header.ps encoding.ps hierarchy.ps

# fig2dev-3.2 is essential
FIG2DEV	     = fig2dev

EXAMPLEDIR   = examples
EXAMPLEFILES = init.c make.c match.c prot.c app1.c app2.c app3.c \
               plac.c
EXAMPLES     = $(patsubst %,$(EXAMPLEDIR)/%,$(EXAMPLEFILES))

doc: at.ps manual.ps

all: 

at.ps: at.dvi $(PICS)

at.dvi: at.ltx aterm.ltx spec/ATerm.mtx $(CHAPS) $(PICS)

manual.dvi: manual.ltx $(EXAMPLES)

# when manual.dvi is up-to-date, it will have created manual.idx
# we need to run makeindex on this file, then re-latex and convert
# the dvi to postscript
manual.ps: manual.dvi
	TEXINPUTS=$$TEXINPUTS:${srcdir};\
	makeindex manual.idx ;\
	latex ${srcdir}/manual.ltx ;\
	dvips -o $@ $<

spell: at.ltx
#	detex at.ltx | spell +words.txt
	detex manual.ltx | spell -b +words.txt

clean:
	rm -f *~ at.aux at.bbl at.blg at.dvi at.log at.ps \
		manual.idx \
		manual.dvi manual.ps \
		encoding.ps header.ps 

dist: at.ps manual.ps
	cp at.ps manual.ps ${DISTDIR}

install:

# end of Makefile
