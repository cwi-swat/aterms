#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include ${top_srcdir}/make_rules

ADTC		= ${top_builddir}/utils/adt-to-c --verbose
ADTJAVA         = ${top_builddir}/utils/adt-to-java --verbose --visitable

JAVATESTS = peano list rolodex builtins
TESTS	  = listtest rolotest builtinstest \
            $(foreach j, ${JAVATESTS}, ${j}.java.sh)

JAVAC=javac

peano_Main = PeanoTest
list_Main  = ListTest
rolodex_Main = RoloTest
builtins_Main = BuiltinsTest

# Building the C programs that are to be run during testing:
check_PROGRAMS	= rolotest listtest builtinstest
check_LIBRARIES	= librolodex.a liblist.a libbuiltins.a

librolodex_a_SOURCES	= rolodex.c rolodex_dict.c
liblist_a_SOURCES	= list.c list_dict.c
libbuiltins_a_SOURCES	= builtins.c builtins_dict.c

rolotest_SOURCES	= rolotest.c
rolotest_LDADD		= -L. -lrolodex -lATerm
rolotest_DEPENDENCIES	= librolodex.a

listtest_SOURCES	= listtest.c
listtest_LDADD		= -L. -llist -lATerm
listtest_DEPENDENCIES	= liblist.a

builtinstest_SOURCES = builtinstest.c
builtinstest_LDADD	= -L. -lbuiltins -lATerm
builtinstest_DEPENDENCIES= libbuiltins.a

CLEANFILES		= ${check_PROGRAMS} ${check_LIBRARIES} \
			rolodex.[ch] rolodex_dict.[ch] \
			list.[ch] list_dict.[ch] \
			builtins.[ch] builtins_dict.[ch] \
			*.java.sh \
			peano/* list/* rolodex/* builtins/* *.class

EXTRA_DIST = rolodex.adt list.adt list.pro peano.adt builtins.adt \
             PeanoTest.java ListTest.java  RoloTest.java rolodex.trm

# Below the generation of Java and C API's:
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

builtins.c builtins.h: builtins.adt 
	${ADTC} \
		--input ${top_srcdir}/test/builtins.adt \
		--classpath ${top_builddir}:${JAVA_ATERM}:${SHARED_OBJECTS}

%.java.sh: Makefile
	${ADTJAVA} --input ${top_srcdir}/test/$*.adt \
	           --output ${top_srcdir}/test/$* \
		   --package test.$* \
		   --name $* \
		   --classpath ${top_builddir}
	(cd ${top_srcdir} ; pwd ;\
         CLASSPATH=.:${top_srcdir}:${JAVA_ATERM}:${SHARED_OBJECTS} \
         ${JAVAC} test/$($*_Main).java)
	@(echo "#! /bin/sh";\
	  echo "CLASSPATH=.:${top_srcdir}:${JAVA_ATERM}:${SHARED_OBJECTS}";\
	  echo "export CLASSPATH";\
          echo "cd ${top_srcdir}/test" ;\
	  echo "java test.$($*_Main) ${top_srcdir}/test") > $@ && chmod +x $@
