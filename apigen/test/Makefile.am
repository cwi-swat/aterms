#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

include ${top_srcdir}/make_rules

ADTC		= ${top_builddir}/utils/adt-to-c
ADTJAVA         = ${top_builddir}/utils/adt-to-java

JAVATESTS = PeanoTest ListTest
TESTS		= rolotest listtest \
	          $(foreach j, ${JAVATESTS}, ${j}.java.sh)

# Building the C programs that are to be run during testing:
check_PROGRAMS	= rolotest listtest
check_LIBRARIES	= librolodex.a liblist.a 

librolodex_a_SOURCES	= rolodex.c rolodex_dict.c
liblist_a_SOURCES	= list.c list_dict.c

rolotest_SOURCES	= rolotest.c
rolotest_LDADD		= -L. -lrolodex -lATerm
rolotest_DEPENDENCIES	= librolodex.a

listtest_SOURCES	= listtest.c
listtest_LDADD		= -L. -llist -lATerm
listtest_DEPENDENCIES	= liblist.a

# Building the Java class files that are to be run during testing:
javadir = ${top_builddir}/test/classfiles
JAVAROOT = ${top_builddir}
CLASSPATH_ENV = CLASSPATH=${top_builddir}:${SHARED_OBJECTS}:${JAVA_ATERM}:${srcdir}/$(JAVAROOT)::$$CLASSPATH

PeanoTestAPI = Nat.java Nat_SucImpl.java  PeanoConstructor.java \
               NatImpl.java  Nat_Zero.java PeanoFactory.java \
               Nat_Suc.java  Nat_ZeroImpl.java

ListTestAPI =  ListFactory.java ListConstructor.java ModulesImpl.java \
               Modules_ListImpl.java Modules_List.java Modules_BodyImpl.java \
               Modules_Body.java Modules.java ModuleListImpl.java \
	       ModuleList_ModulesImpl.java ModuleList_Modules.java \
	       ModuleList.java Module.java ModuleImpl.java \
	       Module_DefaultImpl.java Module_Default.java

check_JAVA = ${PeanoTestAPI} PeanoTest.java \
	     ${ListTestAPI} ListTest.java

CLEANFILES		= ${check_PROGRAMS} ${check_LIBRARIES} \
			rolodex*.[ch] rolodex_dict.[ch] \
			list.[ch] list_dict.[ch] \
			${PeanoTestAPI} ${ListTestAPI} \
			*.java.sh

EXTRA_DIST		= rolodex.adt list.adt list.pro peano.adt

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

%.java.sh: %.java Makefile
	@(echo "#! /bin/sh";\
	  echo "CLASSPATH=${top_builddir}:${JAVA_ATERM}:${SHARED_OBJECTS}";\
	  echo "export CLASSPATH";\
	  echo "java test.$*") > $@ && chmod +x $@

${PeanoTestAPI}: peano.adt
	${ADTJAVA} --input ${top_srcdir}/test/peano.adt \
	           --output ${top_builddir}/test \
		   --package test \
		   --name peano \
		   --classpath ${top_builddir}

${ListTestAPI}: list.adt
	${ADTJAVA} --input ${top_srcdir}/test/list.adt \
	           --output ${top_builddir}/test \
		   --package test \
		   --name list \
		   --classpath ${top_builddir}
