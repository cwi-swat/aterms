EXTRA_DIST = append.ltx at.ltx aterm.ltx corrections.ltx \
             corrections2.ltx gtld-at.ltx manual.ltx thesis.bib \
             encoding.fig header.fig hierarchy.fig sharing.eps \
             speauth.cls spedoc.tex \
             general-make at.ps manual.ps java-api.ps

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

JAVA_CLASSES	= AFun ATerm ATermInt ATermReal ATermBlob ATermAppl \
		ATermList ATermPlaceholder ATermFactory
JAVA_FILES	= $(patsubst %,${top_srcdir}/java/aterm/%.java,$(JAVA_CLASSES))

doc: at.ps manual.ps java-api.ps

# This uses the TexDoclet available at c2_tech.com
java-api.ltx: $(JAVA_FILES)
	javadoc -public \
		-doclet com.c2_tech.doclets.TexDoclet \
		-docletpath ${TEXDOCLET} \
		-output java-api.ltx \
		-noinherited \
		$(JAVA_FILES)

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

java-api.dvi: java-api.ltx

java-api.ps: java-api.dvi
	dvips -o $@ $<

clean:
	rm -f *~ at.aux at.bbl at.blg at.dvi at.log at.ps \
		manual.idx \
		manual.dvi manual.ps \
		encoding.ps header.ps 

dist: at.ps manual.ps
	cp at.ps manual.ps ${DISTDIR}

install:

# end of Makefile
