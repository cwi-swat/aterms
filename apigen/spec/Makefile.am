#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

SDF2TABLE	= ${PGENBIN}/sdf2table

SUBDIRS		= gen-c-api sdf2-to-adt

data_DATA	= Gen-C-API.trm.tbl Sdf2-to-ADT.trm.tbl

EXTRA_DIST	= ${GEN_C_API_SDF} ${SDF2_TO_ADT_SDF}

CLEAN_FILES	= ${data_DATA}

SDF2_SPEC_SDF  	= $(patsubst %, ./sdf2/%, $(SDF2_SPEC:=.sdf)) 
SDF2_SPEC	= ATermBooleans ATerms Alias-Sdf-Syntax Basic-Sdf-Syntax \
		CC-Sdf-Syntax Character-Class-Syntax Character-Syntax \
	       	Grammar-Syntax IntCon Kernel-Sdf-Syntax Label-Sdf-Syntax \
	       	Layout Literals-Sdf-Syntax Literals Modular-Sdf-Syntax \
		Priority-Sdf-Syntax RealCon Regular-Sdf-Syntax \
		Renaming-Sdf-Syntax Restrictions-Sdf-Syntax Sdf2-Syntax \
		Sdf2 Sorts-Sdf-Syntax Symbols

GEN_C_API_SDF	= $(GEN_C_API:=.sdf)
GEN_C_API	= ATerm-Path ATerm-Syntax ATerm-Utils C-Identifiers \
		C-Integers C-Layout C-Strings C-Syntax Gen-API Gen-C-API \
		Integer-Syntax Literal-Syntax Naturals

SDF2_TO_ADT_SDF	= $(SDF2_TO_ADT:=.sdf)
SDF2_TO_ADT	= ATerm-Utils Sdf2-to-ADT

Gen-C-API.trm.tbl: $(GEN_C_API_SDF)
	@echo Building parsetable: $@
	@(printf "definition\nmodule Main\nimports Gen-C-API\n"; \
	 cat $(GEN_C_API_SDF)) | ${SDF2TABLE} -o $@

Sdf2-to-ADT.trm.tbl: $(SDF2_TO_ADT_SDF) $(SDF2_SPEC_SDF)
	@echo Building parsetable: $@
	@(printf "definition\nmodule Main\nimports Sdf2-to-ADT\n"; \
	 cat $(SDF2_TO_ADT_SDF); \
	 cat $(SDF2_SPEC_SDF)) | \
	${SDF2TABLE} -o $@
