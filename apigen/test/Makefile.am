#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

SGLR			= ${SGLRBIN}/sglr
SDF2TABLE		= ${PGENBIN}/sdf2table

PROGS			= rolotest booltest

TESTS			= rolotest booltest

CLEANFILES		= librolodex.a libBooleans.a booltest.af1 Booleans.trm.tbl \
			 Booleans.adt ${PROGS}

INCLUDES		= -I${ATERMINC} -Irolodex -IBooleans
LDFLAGS			= -L${ATERMLIB}
LDADD			= -lATerm

EXTRA_DIST		= rolodex.adt rolodex.pro Booleans.sdf Booleans.pro

noinst_PROGRAMS		= ${PROGS}
rolotest_SOURCES	= rolotest.c
rolotest_LDADD		= librolodex.a ${LDADD}
rolotest_DEPENDENCIES	= librolodex.a

booltest_SOURCES	= booltest.c
booltest_LDADD		= libBooleans.a ${LDADD}
booltest_DEPENDENCIES	= booltest.af1 libBooleans.a

# Needed for first time making (when no .deps dir exists)
rolotest.o: rolodex/rolodex.h
booltest.o: Booleans/Booleans.h

librolodex.a rolodex/rolodex.h: rolodex.adt rolodex.pro
	${top_srcdir}/utils/gen-c-api rolodex 1.0
	@cd rolodex; \
	./reconf; \
	./configure --with-aterm-inc=${ATERMINC}; \
	${MAKE}
	@cp rolodex/librolodex.a .
	
libBooleans.a Booleans/Booleans.h: Booleans.sdf Booleans.pro
	${top_srcdir}/utils/sdf2-to-adt Booleans 1.0
	@cd Booleans; \
	./reconf; \
	./configure --with-aterm-inc=${ATERMINC}; \
	${MAKE}
	@cp Booleans/libBooleans.a .

Booleans.trm.tbl: Booleans.sdf.unlabeled
	${SDF2TABLE} -i $< -o $@

booltest.af1: Booleans.trm.tbl booltest.trm
	${SGLR} -1 -p Booleans.trm.tbl -i booltest.trm -o $@


