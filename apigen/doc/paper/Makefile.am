#
# Makefile.am
#
# $Id$
#

SPECS = ATerms.sdf Symbol.sdf Tree.sdf Attributes.sdf
EXAMPLES = bool.example set.example
SOURCES = apigen.ltx ${SPECS} ${EXAMPLES}

include ${top_srcdir}/make_rules

doc: apigen.ps

apigen.ps: apigen.dvi

apigen.dvi: $(SOURCES)

CLEANFILES = *.aux *.blg *.dvi *.log *.ps

# end of Makefile.am
