#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

SDF2TABLE	= ${PGENBIN}/sdf2table

SUBDIRS		= sdf2-to-adt sdf2-to-asfix2me-adt add-labels

pkgdata_DATA	= Sdf2-to-ADT.trm.tbl
CLEANFILES	= ${data_DATA}

EXTRA_DIST	= ${SDF2_TO_ADT_SDF} ${SDF2_SPEC_SDF} Sdf2-to-ADT.trm.tbl

SDF2_SPEC_SDF  	= $(patsubst %, ./sdf2/%, $(SDF2_SPEC:=.sdf)) 
SDF2_SPEC	= Alias-Sdf-Syntax Basic-Sdf-Syntax \
		CC-Sdf-Syntax Character-Class-Syntax Character-Syntax \
		Grammar-Syntax Kernel-Sdf-Syntax Label-Sdf-Syntax Layout \
	       	Literals Literals-Sdf-Syntax Modular-Sdf-Syntax \
		Priority-Sdf-Syntax Regular-Sdf-Syntax Renaming-Sdf-Syntax \
		Restrictions-Sdf-Syntax Sdf2 Sdf2-Syntax Sorts-Sdf-Syntax \
		Symbols

SDF2_TO_ADT_SDF	= $(SDF2_TO_ADT:=.sdf)
SDF2_TO_ADT	= ATerm-Utils ATerm-Syntax Literal-Syntax Integer-Syntax \
		Sdf2-to-ADT

Sdf2-to-ADT.trm.tbl: $(SDF2_TO_ADT_SDF) $(SDF2_SPEC_SDF)
	@echo Building parsetable: $@
	@(printf "definition\nmodule Main\nimports Sdf2-to-ADT\n"; \
	 cat $(SDF2_TO_ADT_SDF); \
	 cat $(SDF2_SPEC_SDF)) \
	| ${SDF2TABLE} -o $@
