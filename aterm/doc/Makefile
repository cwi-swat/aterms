include general-make

CHAPS	= at.ltx
PICS	= header.ps encoding.ps

EXAMPLEDIR		= examples
EXAMPLEFILES	= init.c prot.c
EXAMPLES		= $(patsubst %,$(EXAMPLEDIR)/%,$(EXAMPLEFILES))

all: at.ps manual.ps

at.ps: at.dvi $(PICS)
	dvips at.dvi

at.dvi: at.ltx ATerms.mtx $(CHAPS) $(PICS)

manual.ps: manual.dvi
	dvips manual.dvi

manual.dvi: manual.ltx $(EXAMPLES)

header.ps: header.fig
	fig2dev -Lps header.fig header.ps

encoding.ps: encoding.fig
	fig2dev -Lps encoding.fig encoding.ps

spell: at.ltx
	detex at.ltx | spell +words.txt

clean:
	rm -f *~ at.aux at.bbl at.blg at.dvi at.log at.ps \
		encoding.ps header.ps 

# end of Makefile
