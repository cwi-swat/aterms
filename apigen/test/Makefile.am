#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

PROGS	= rolotest

TESTS	= rolotest

INCLUDES		= -I${ATERM}/include -Irolodex
LDFLAGS			= -L${ATERM}/lib

noinst_PROGRAMS		= ${PROGS}
rolotest_SOURCES	= rolotest.c
rolotest_LDADD		= librolodex.a -lATerm

rolotest.o: rolodex/rolodex.h

CLEANFILES		= librolodex.a ${PROGS}

librolodex.a rolodex/rolodex.h: rolodex.adt rolodex.pro
	${top_srcdir}/utils/apigen rolodex 1.0
	@cd rolodex; \
	./reconf; \
	./configure --with-aterm=${ATERM}; \
	${MAKE}
	@cp rolodex/librolodex.a .
	
