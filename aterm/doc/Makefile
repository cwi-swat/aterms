include general-make

CHAPS	= at.ltx
PICS	= header.ps encoding.ps

EXAMPLEDIR		= examples
EXAMPLEFILES	= init.c prot.c
EXAMPLES		= $(patsubst %,$(EXAMPLEDIR)/%,$(EXAMPLEFILES))

all: at.ps manual.ps

at.ps: at.dvi $(PICS)
	dvips at.dvi

at.dvi: at.ltx spec/ATerm.mtx $(CHAPS) $(PICS)

manual.dvi: manual.ltx $(EXAMPLES)

# when manual.dvi is up-to-date, it will have created manual.idx
# we need to run makeindex on this file, then re-latex and convert
# the dvi to postscript
manual.ps: manual.dvi
	makeindex manual.idx
	latex manual.ltx
	dvips manual.dvi

header.ps: header.fig
	fig2dev -Lps header.fig header.ps

encoding.ps: encoding.fig
	fig2dev -Lps encoding.fig encoding.ps

spell: at.ltx
#	detex at.ltx | spell +words.txt
	detex manual.ltx | spell -b +words.txt

clean:
	rm -f *~ at.aux at.bbl at.blg at.dvi at.log at.ps \
		manual.idx \
		manual.dvi manual.ps \
		encoding.ps header.ps 

# end of Makefile
