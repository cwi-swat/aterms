#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include ${top_srcdir}/make_rules

ADTC		= ${top_builddir}/utils/adt-to-c
ADTJAVA         = ${top_builddir}/utils/adt-to-java

TESTS			= rolotest listtest peanotest.sh

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

javadir = ${top_builddir}/test/classfiles
JAVAROOT = ${top_builddir}
CLASSPATH_ENV = CLASSPATH=${top_builddir}:${SHARED_OBJECTS}:${JAVA_ATERM}:${srcdir}/$(JAVAROOT)::$$CLASSPATH

java_JAVA = PeanoTest.java

CLEANFILES		= ${noinst_PROGRAMS} ${noinst_LIBRARIES} \
			rolodex*.[ch] rolodex_dict.[ch] \
			list.[ch] list_dict.[ch] \
			PeanoFactory.java PeanoConstructor.java \
			Nat*.java

EXTRA_DIST		= rolodex.adt list.adt list.pro peano.adt

rolodex.c rolodex.h: rolodex.adt
	${ADTC} \
		--input ${top_srcdir}/test/rolodex.adt \
		--compat-term \
		--classpath ${top_builddir}:${JAVA_ATERM}:${SHARED_OBJECTS}

list.c list.h: list.adt list.pro
	${ADTC} \
		--input ${top_srcdir}/test/list.adt \
		--prologue ${top_srcdir}/test/list.pro \
		--compat-term \
		--classpath ${top_builddir}:${JAVA_ATERM}:${SHARED_OBJECTS}

peanotest.sh: PeanoTest.java peano.adt Makefile
	${ADTJAVA} --input ${top_srcdir}/test/peano.adt \
	           --output ${top_builddir}/test \
		   --package test \
		   --name peano
	@(echo "#! /bin/sh";\
	  echo "CLASSPATH=${top_builddir}:${JAVA_ATERM}:${SHARED_OBJECTS}";\
	  echo "export CLASSPATH";\
	  echo "java test.PeanoTest") > $@ && chmod +x $@

