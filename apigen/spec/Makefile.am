#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

SUBDIRS		= add-labels

bin_PROGRAMS	= Sdf2-to-ADT Sdf2-to-Imploded-ADT

CFLAGS		= -g -Wall -Wno-unused -O2 -DASF_MAIN

INCLUDES	= -I$(ASCINC) \
		-I$(PT_SUPPORT)/include \
		-I$(ASF_SUPPORT)/include \
		-I$(ATERM)/include

pkgdata_DATA	= Sdf2-to-ADT.trm.tbl
CLEANFILES	= ${pkgdata_DATA}

EXTRA_DIST	= ${SDF2_TO_ADT_SDF} ${SDF2_SPEC_SDF} \
                  ${UTILS_SPEC_SDF} Sdf2-to-ADT.trm.tbl

SDF2TABLE	= ${PGENBIN}/sdf2table

UTILS_SPEC_SDF  = $(patsubst %, ./utils/%, $(UTILS_SPEC:=.sdf)) 
UTILS_SPEC	= Basic-Integers Booleans Integers

SDF2_SPEC_SDF  	= $(patsubst %, ./sdf2/%, $(SDF2_SPEC:=.sdf)) 
SDF2_SPEC	= Alias-Sdf-Syntax Basic-Sdf-Syntax \
		CC-Sdf-Syntax Character-Class-Syntax Character-Syntax \
		Grammar-Syntax Kernel-Sdf-Syntax Label-Sdf-Syntax Layout \
	       	Literals Literals-Sdf-Syntax Modular-Sdf-Syntax \
		Priority-Sdf-Syntax Regular-Sdf-Syntax Renaming-Sdf-Syntax \
		Restrictions-Sdf-Syntax Sdf2 Sdf2-Syntax Sorts-Sdf-Syntax \
		Symbols Character-Class-Membership \
		Character-Class-Normalization Character-Arithmetic \
		Character-Normalization IntCon

SDF2_TO_ADT_SDF	= $(SDF2_TO_ADT:=.sdf)
SDF2_TO_ADT	= ATerm-Utils ATerm-Syntax Literal-Syntax \
		Integer-Syntax Sdf2-to-AsFix2ME-ADT \
		Build-General-AsFix2ME-ADT Build-AsFix2ME-ADT Cons-Options

Sdf2_to_ADT_SOURCES	= Sdf2-to-AsFix2ME-ADT.c
Sdf2_to_ADT_LDADD	= -L$(ASCLIB) -lasc-support-me \
			-L$(ASF_SUPPORT)/lib -lASFME \
			-L$(PT_SUPPORT)/lib -lmept \
			-L$(ATERM)/lib -lATerm
Sdf2_to_Imploded_ADT_SOURCES = Sdf2-to-Imploded-ADT.c
Sdf2_to_Imploded_ADT_LDADD = -L$(ASCLIB) -lasc-support-me \
			-L$(ASF_SUPPORT)/lib -lASFME \
			-L$(PT_SUPPORT)/lib -lmept \
			-L$(ATERM)/lib -lATerm


Sdf2-to-ADT.trm.tbl: $(SDF2_TO_ADT_SDF) $(SDF2_SPEC_SDF) $(UTILS_SPEC_SDF) 
	@echo Building parsetable: $@
	@(printf "definition\nmodule Main\nimports Sdf2-to-AsFix2ME-ADT\n"; \
	 cat $(SDF2_TO_ADT_SDF); \
	 cat $(SDF2_SPEC_SDF); \
	 cat $(UTILS_SPEC_SDF)) \
	| ${SDF2TABLE} -o $@
