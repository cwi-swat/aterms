#
# Makefile.am
#
# $Id$
#

SPECS = ATerms.sdf Symbol.sdf Tree.sdf Attributes.sdf
FIGS = old-new.eps gen.eps names.eps
EXAMPLES = bool.example set.example bool_and_dict
SOURCES = apigen.ltx $(SPECS) $(EXAMPLES) $(FIGS)

include ${top_srcdir}/make_rules

doc: apigen.ps

apigen.ps: apigen.dvi $(PICS)

apigen.dvi: $(SOURCES) $(PICS)

CLEANFILES = *.aux *.blg *.dvi *.log *.ps

# end of Makefile.am
