#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

PROGS	= rolotest booltest

TESTS	= rolotest booltest

CLEANFILES		= librolodex.a libbooleans.a ${PROGS}

INCLUDES		= -I${ATERM}/include -Irolodex -Ibooleans
LDFLAGS			= -L${ATERM}/lib

EXTRA_DIST		= rolodex.adt rolodex.pro booleans.adt booleans.pro

noinst_PROGRAMS		= ${PROGS}
rolotest_SOURCES	= rolotest.c
rolotest_LDADD		= librolodex.a -lATerm

booltest_SOURCES	= booltest.c
booltest_LDADD		= libbooleans.a -lATerm

rolotest.o: rolodex/rolodex.h

booltest.o: booleans/booleans.h

librolodex.a rolodex/rolodex.h: rolodex.adt rolodex.pro
	${top_srcdir}/utils/apigen rolodex 1.0
	@cd rolodex; \
	./reconf; \
	./configure --with-aterm=${ATERM}; \
	${MAKE}
	@cp rolodex/librolodex.a .
	
libbooleans.a booleans/booleans.h: booleans.adt booleans.pro
	${top_srcdir}/utils/apigen booleans 1.0
	@cd booleans; \
	./reconf; \
	./configure --with-aterm=${ATERM}; \
	${MAKE}
	@cp booleans/libbooleans.a .

