# $Id$

SPECS = ATerms.sdf Symbol.sdf Tree.sdf Attributes.sdf
FIGS = old-new.eps gen.eps names.eps
EXAMPLES = bool.example set.example bool_and_dict
SOURCES = jlap.ltx $(SPECS) $(EXAMPLES) $(FIGS)

include ${top_srcdir}/make_rules

doc: cwi.ps jlap.ps

jlap.ps: jlap.dvi $(PICS)
jlap.dvi: $(SOURCES) $(PICS)

cwi.ps: cwi.dvi $(PICS)
cwi.dvi: $(SOURCES) $(PICS)

CLEANFILES = *.aux *.blg *.dvi *.log *.ps
