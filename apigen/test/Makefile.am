#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

SGLR			= ${SGLRBIN}/sglr
SDF2TABLE		= ${PGENBIN}/sdf2table

PROGS			= rolotest booltest listtest
NOINST_PROGS		= ${PROGS}

TESTS			= rolotest booltest listtest

lib_LIBRARIES		= librolodex.a liblist.a libBooleans.a
CLEANFILES		= ${PROGS} \
			librolodex.a rolodex*.[ch] rolodex_dict.[ch] \
			liblist.a list.[ch] list_dict.[ch] \
			libBooleans.a Booleans.[ch] Booleans.dict \
			Booleans_dict.[ch] Booleans.trm.tbl Booleans.adt \
			booltest.af1

INCLUDES		= -I${ATERMINC}
LDFLAGS			= -L${ATERMLIB}
LDADD			= -lATerm
CFLAGS			= -Wall -g -O2

EXTRA_DIST		= rolodex.adt Booleans.sdf list.adt list.pro \
			booltest.trm

noinst_PROGRAMS		= ${PROGS}

librolodex_a_SOURCES	= rolodex.c rolodex_dict.c
liblist_a_SOURCES	= list.c list_dict.c
libBooleans_a_SOURCES	= Booleans.c Booleans_dict.c

rolotest_SOURCES	= rolotest.c
rolotest_LDADD		= librolodex.a ${LDADD}
rolotest_DEPENDENCIES	= librolodex.a

listtest_SOURCES	= listtest.c
listtest_LDADD		= liblist.a ${LDADD}
listtest_DEPENDENCIES	= liblist.a

booltest_SOURCES	= booltest.c
booltest_LDADD		= libBooleans.a ${LDADD}
booltest_DEPENDENCIES	= booltest.af1 libBooleans.a

# Contrary to some other .adt files in this directory, Booleans.adt is
# generated and should be considered intermediate
%INTERMEDIATE: Booleans.adt

rolodex.c rolodex.h: rolodex.adt
	${top_srcdir}/utils/adt-to-c	\
		--input rolodex.adt	\
		--classpath ${top_srcdir}:${ATERM}/classes

list.c list.h: list.adt list.pro
	${top_srcdir}/utils/adt-to-c	\
		--input list.adt	\
		--prologue list.pro	\
		--classpath ${top_srcdir}:${ATERM}/classes

Booleans.c Booleans.h: Booleans.adt
	${top_srcdir}/utils/adt-to-c	\
		--input Booleans.adt	\
		--prefix SDF		\
		--classpath ${top_srcdir}:${ATERM}/classes

booltest.af1: Booleans.trm.tbl booltest.trm
	${SGLR} -1 -p Booleans.trm.tbl -i booltest.trm -o $@

%.adt: %.sdf
	${top_srcdir}/utils/sdf2-to-adt \
		--input $< \
		--output $@ \
		--table ${top_srcdir}/spec/Sdf2-to-ADT.trm.tbl \
		--label ${top_srcdir}/spec/add-labels/AddLabels.asf \
		--generator ${top_srcdir}/spec/sdf2-to-adt/Sdf2-to-ADT

%.trm.tbl: %.sdf
	${SDF2TABLE} -m `basename $< .sdf` -i $< -o $@
