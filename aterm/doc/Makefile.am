include ${srcdir}/general-make

SUBDIRS		= examples spec

EXTRA_DIST	= append.ltx at.ltx aterm.ltx corrections.ltx \
		corrections2.ltx gtld-at.ltx manual.ltx thesis.bib \
		encoding.fig header.fig hierarchy.fig sharing.eps \
		speauth.cls spedoc.tex general-make at.ps manual.ps \
		appl-enc.fig 

CLEANFILES	= encoding.eps header.eps hierarchy.eps \
		at.{aux,bbl,blg,dvi,log,ps} \
		manual.{aux,bbl,blg,dvi,idx,ps,ilg,ind,log,toc} \
		sigplan.{aux,bbl,blg,log,dvi,ps} \
		xootic.{aux,bbl,blg,log,dvi,ps}

CHAPS		= at.ltx
AT_PICS		= header.eps encoding.eps hierarchy.eps
SP_PICS		= appl-enc.eps
XO_PICS		= header.eps

FIG2DEV		= fig2dev
EXAMPLEDIR	= examples
EXAMPLEFILES	= init.c make.c match.c prot.c app1.c app2.c app3.c plac.c
EXAMPLES	= $(patsubst %,$(EXAMPLEDIR)/%,$(EXAMPLEFILES))

at.ps:
	$(MAKE) at.dvi
	$(MAKE) $(AT_PICS)
	dvips -o $@ at.dvi

# when manual.dvi is up-to-date, the file manual.idx will have been
# generated, and we need to run makeindex on this file, then re-latex and
# convert the dvi to postscript

manual.ps:
	$(MAKE) manual.dvi
	TEXINPUTS=$$TEXINPUTS:${srcdir};\
	makeindex manual.idx ;\
	latex ${srcdir}/manual.ltx ;\
	dvips -o $@ manual.dvi

at.dvi: at.ltx aterm.ltx spec/ATerm.mtx $(CHAPS) $(AT_PICS)
manual.dvi: manual.ltx $(EXAMPLES)
