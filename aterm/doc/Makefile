include general-make

CHAPS	= at.ltx
PICS	= header.ps encoding.ps

EXAMPLEDIR		= examples
EXAMPLEFILES	= init.c make.c match.c prot.c app1.c app2.c app3.c \
				  plac.c
EXAMPLES		= $(patsubst %,$(EXAMPLEDIR)/%,$(EXAMPLEFILES))

doc: at.ps manual.ps

all: 

at.ps: at.dvi $(PICS)

at.dvi: at.ltx aterm.ltx spec/ATerm.mtx $(CHAPS) $(PICS)

manual.dvi: manual.ltx $(EXAMPLES)

# when manual.dvi is up-to-date, it will have created manual.idx
# we need to run makeindex on this file, then re-latex and convert
# the dvi to postscript
manual.ps: manual.dvi
	makeindex manual.idx
	latex manual.ltx
	dvips -o $@ $<

# You need fig2dev version 3.2 for this.
header.ps: header.fig
	/home/mdejonge/bin/fig2dev-3.2 -Lps header.fig header.ps

# You need fig2dev version 3.2 for this.
encoding.ps: encoding.fig
	/home/mdejonge/bin/fig2dev-3.2 -Lps encoding.fig encoding.ps

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
