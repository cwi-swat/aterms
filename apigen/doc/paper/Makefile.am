#
# Makefile.am
#
# $Id$
#

SOURCES = apigen.ltx aterm.sdf bool.example set.example

include ${top_srcdir}/make_rules

doc: apigen.ps

apigen.ps: apigen.dvi

apigen.dvi: $(SOURCES)

CLEANFILES = *.aux *.blg *.dvi *.log *.ps

# end of Makefile.am
