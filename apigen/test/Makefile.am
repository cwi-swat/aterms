#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include ${top_srcdir}/make_rules

SGLR			= ${SGLRBIN}/sglr
SDF2TABLE		= ${PGENBIN}/sdf2table
ADT_TO_C		= ${top_builddir}/utils/adt-to-c
SDF2_TO_ADT		= ${top_builddir}/utils/sdf2-to-adt
GENERATOR		= ${top_builddir}/spec/Sdf2-to-ADT
PARSETABLE		= ${top_srcdir}/spec/Sdf2-to-ADT.trm.tbl 
LABEL_EQS		= ${top_srcdir}/spec/add-labels/AddLabels.eqs 

TESTS			= rolotest booltest listtest
noinst_PROGRAMS		= ${TESTS}
noinst_LIBRARIES	= librolodex.a liblist.a libBooleans.a

librolodex_a_SOURCES	= rolodex.c rolodex_dict.c
liblist_a_SOURCES	= list.c list_dict.c
libBooleans_a_SOURCES	= Booleans.c Booleans_dict.c

rolotest_SOURCES	= rolotest.c
rolotest_LDADD		= -L. -lrolodex -lATerm
rolotest_DEPENDENCIES	= librolodex.a

listtest_SOURCES	= listtest.c
listtest_LDADD		= -L. -llist -lATerm
listtest_DEPENDENCIES	= liblist.a

booltest_SOURCES	= booltest.c
booltest_LDADD		= -L. -lBooleans -L$(PT_SUPPORT)/include -lmept -L$(ATERM)/include -lATerm
booltest_DEPENDENCIES	= booltest.pt libBooleans.a

CLEANFILES		= ${noinst_PROGRAMS} ${noinst_LIBRARIES} \
			rolodex*.[ch] rolodex_dict.[ch] \
			list.[ch] list_dict.[ch] \
			Booleans.[ch] Booleans.dict \
			Booleans_dict.[ch] Booleans.trm.tbl Booleans.adt \
			booltest.pt

EXTRA_DIST		= rolodex.adt Booleans.sdf Booleans.pro list.adt list.pro \
			booltest.trm

rolodex.c rolodex.h: rolodex.adt
	${ADT_TO_C} \
		--input ${top_srcdir}/test/rolodex.adt \
		--compat-term \
		--classpath ${top_builddir}:${ATERM}/classes

list.c list.h: list.adt list.pro
	${ADT_TO_C} \
		--input ${top_srcdir}/test/list.adt \
		--prologue ${top_srcdir}/test/list.pro \
		--compat-term \
		--classpath ${top_builddir}:${ATERM}/classes

Booleans.c Booleans.h: Booleans.adt
	${ADT_TO_C} \
		--input Booleans.adt \
		--prologue ${top_srcdir}/test/Booleans.pro \
		--prefix SDF \
		--compat-term \
		--classpath ${top_builddir}:${ATERM}/classes

booltest.pt: Booleans.trm.tbl booltest.trm
	${SGLR} -p Booleans.trm.tbl -i ${top_srcdir}/test/booltest.trm -o $@

%.adt: %.sdf ${PARSETABLE} ${LABEL_EQS} ${GENERATOR}
	${SDF2_TO_ADT} \
		--input $< \
		--output $@ \
		--table ${PARSETABLE} \
		--label ${LABEL_EQS} \
		--generator ${GENERATOR}

%.trm.tbl: %.sdf
	${SDF2TABLE} -m `basename $< .sdf` -i $< -o $@
