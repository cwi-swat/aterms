# $Id$

include $(top_srcdir)/Makefile.meta

ADTTOC = ${top_builddir}/scripts/adt-to-c-test

ADTTOJAVA = ${top_builddir}/scripts/adt-to-java-test

JAVATESTS = cyclic peano list builtins

TESTS = listtest builtinstest \
            $(foreach j, ${JAVATESTS}, ${j}.java.sh)

JAVAC = javac

peano_Main = PeanoTest
list_Main  = ListTest
builtins_Main = BuiltinsTest
cyclic_Main = CyclicTest

# Build the C programs that are to be run during testing:
check_PROGRAMS	= listtest builtinstest

listtest_SOURCES = listtest.c list.c list_dict.c
listtest_CPPFLAGS = $(ATERM_CFLAGS)
listtest_LDADD = $(ATERM_LIBS)

builtinstest_SOURCES = builtinstest.c builtins.c builtins_dict.c
builtinstest_CPPFLAGS = $(ATERM_CFLAGS)
builtinstest_LDADD = $(ATERM_LIBS)

distclean-local: clean-local

clean-local: 
	rm -rf $(JAVATESTS)

CLEANFILES += \
	$(check_PROGRAMS) $(check_LIBRARIES) \
	list.[ch] list_dict.[ch] \
	builtins.[ch] builtins_dict.[ch] \
	*.java.sh *.class *.jar *.dict

BUILT_SOURCES = \
	list.c \
	list.h \
	list_dict.c \
	list_dict.h \
	builtins.c \
	builtins.h \
	builtins_dict.c \
	builtins_dict.h 

EXTRA_DIST += \
	BuiltinsTest.java \
	GenericTraversal.java \
	ListTest.java \
	CyclicTest.java \
	PeanoTest.java \
	Replace.java \
	RoloTest.java \
	builtins.adt \
	list.adt \
	cyclic.adt \
	list.pro \
	peano.adt 

list.c list.h: list.adt list.pro
	$(ADTTOC) \
		--input ${top_srcdir}/test/list.adt \
		--name list \
		--prologue ${top_srcdir}/test/list.pro \
		--term-compatibility

builtins.c builtins.h: builtins.adt 
	$(ADTTOC) \
		--input ${top_srcdir}/test/builtins.adt \
		--name builtins

%.java.sh: Makefile
	$(ADTTOJAVA) \
		--input ${top_srcdir}/test/$*.adt \
		--name $* \
		--output ${top_builddir} \
		--package test \
		--version 0.1 \
		--visitable \
		--nojar
	$(JAVAC) \
		-classpath ${top_srcdir}:${top_builddir}:${ATERM_JAVA_JARS} \
		${top_srcdir}/test/$($*_Main).java \
		-d ${top_builddir}
	@(echo "#! /bin/sh"; \
		echo "CLASSPATH=.:${top_builddir}:${ATERM_JAVA_JARS}:${EXTERNAL_JARS}"; \
	  echo "export CLASSPATH"; \
	  echo "java test.$($*_Main) ${top_srcdir}/test") \
	> $@ \
	&& chmod +x $@
