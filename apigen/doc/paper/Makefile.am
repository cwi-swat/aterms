# $Id$

include $(top_srcdir)/doc/paper/make_rules

SPECS = ATerms.sdf Symbol.sdf Tree.sdf Attributes.sdf
FIGS = old-new.eps gen.eps names.eps
EXAMPLES = bool.example set.example bool_and_dict
SOURCES = preamble.ltx abstract.ltx apigen.ltx $(SPECS) $(EXAMPLES) $(FIGS)

EXTRA_DIST = $(SOURCES) jlap.ltx jlap.ps elsart.cls apigen.bib

CLEANFILES = *.aux *.blg *.dvi *.log *.ps

all:

doc: cwi.ps jlap.ps

jlap.ps: jlap.dvi $(PICS)
jlap.dvi: jlap.ltx $(SOURCES) $(PICS)

cwi.ps: cwi.dvi $(PICS)
cwi.dvi: cwi.ltx $(SOURCES) $(PICS)
