# $Id$

SPECS = ATerms.sdf Symbol.sdf Tree.sdf Attributes.sdf
FIGS = old-new.eps gen.eps names.eps
EXAMPLES = bool.example set.example bool_and_dict
SOURCES = abstract.ltx apigen.ltx $(SPECS) $(EXAMPLES) $(FIGS)

include ${top_srcdir}/make_rules

doc: cwi.ps jlap.ps

jlap.ps: jlap.dvi $(PICS)
jlap.dvi: jlap.ltx $(SOURCES) $(PICS)

cwi.ps: cwi.dvi $(PICS)
cwi.dvi: cwi.ltx $(SOURCES) $(PICS)

CLEANFILES = *.aux *.blg *.dvi *.log *.ps
