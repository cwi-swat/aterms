include ${srcdir}/general-make

SUBDIRS		= examples spec

EXTRA_DIST	= append.ltx at.ltx aterm.ltx corrections.ltx \
		corrections2.ltx gtld-at.ltx manual.ltx thesis.bib \
		encoding.fig header.fig hierarchy.fig sharing.eps \
		speauth.cls spedoc.tex general-make at.ps manual.ps \
		appl-enc.fig $(JAVA_API_PS)

CLEANFILES	= *~ encoding.eps header.eps \
		at.{aux,bbl,blg,dvi,log,ps} \
		java-api.{aux,dvi,log,ps,toc} \
		manual.{aux,bbl,blg,ilg,ind,log,toc} \
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

JAVA_CLASSES	= AFun ATerm ATermInt ATermReal ATermBlob ATermAppl \
		ATermList ATermPlaceholder ATermFactory
JAVA_FILES	= $(patsubst %,${top_srcdir}/java/aterm/%.java,$(JAVA_CLASSES))

all:

doc: xootic.ps sigplan.ps at.ps manual.ps $(JAVA_API_PS)

xootic.ps: xootic.dvi $(XO_PICS)
xootic.dvi: xootic.ltx $(XO_PICS)

sigplan.ps: sigplan.dvi $(SP_PICS)
sigplan.dvi: sigplan.ltx asm-diff.ltx $(SP_PICS)

at.ps: at.dvi $(AT_PICS)
at.dvi: at.ltx aterm.ltx spec/ATerm.mtx $(CHAPS) $(AT_PICS)

manual.dvi: manual.ltx $(EXAMPLES)

# when manual.dvi is up-to-date, the file manual.idx will have been
# generated, and we need to run makeindex on this file, then re-latex and
# convert the dvi to postscript

manual.ps: manual.dvi
	TEXINPUTS=$$TEXINPUTS:${srcdir};\
	makeindex manual.idx ;\
	latex ${srcdir}/manual.ltx ;\
	dvips -o $@ $<

# This uses the TexDoclet available at c2_tech.com
java-api.ltx: $(JAVA_FILES)
	javadoc -public \
		-doclet com.c2_tech.doclets.TexDoclet \
		-docletpath $(TEX_DOCLET) \
		-output java-api.ltx \
		-noinherited \
		$(JAVA_FILES)

java-api.dvi: java-api.ltx
java-api.ps: java-api.dvi
	dvips -o $@ $<

dist: at.ps manual.ps
	cp at.ps manual.ps ${DISTDIR}

install:
