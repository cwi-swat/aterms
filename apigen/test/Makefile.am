#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include ${top_srcdir}/make_rules

ADT_TO_C		= ${top_builddir}/utils/adt-to-c

TESTS			= rolotest listtest 

noinst_PROGRAMS		= ${TESTS}
noinst_LIBRARIES	= librolodex.a liblist.a 

librolodex_a_SOURCES	= rolodex.c rolodex_dict.c
liblist_a_SOURCES	= list.c list_dict.c

rolotest_SOURCES	= rolotest.c
rolotest_LDADD		= -L. -lrolodex -lATerm
rolotest_DEPENDENCIES	= librolodex.a

listtest_SOURCES	= listtest.c
listtest_LDADD		= -L. -llist -lATerm
listtest_DEPENDENCIES	= liblist.a

CLEANFILES		= ${noinst_PROGRAMS} ${noinst_LIBRARIES} \
			rolodex*.[ch] rolodex_dict.[ch] \
			list.[ch] list_dict.[ch] 

EXTRA_DIST		= rolodex.adt list.adt list.pro 

rolodex.c rolodex.h: rolodex.adt
	${ADT_TO_C} \
		--input ${top_srcdir}/test/rolodex.adt \
		--compat-term \
		--classpath ${top_builddir}:${JAVA_ATERM}:${SHARED_OBJECTS}

list.c list.h: list.adt list.pro
	${ADT_TO_C} \
		--input ${top_srcdir}/test/list.adt \
		--prologue ${top_srcdir}/test/list.pro \
		--compat-term \
		--classpath ${top_builddir}:${JAVA_ATERM}:${SHARED_OBJECTS}

