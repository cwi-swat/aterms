javadir         = ${top_builddir}/test/classfiles
JAVAROOT        = ${top_builddir}
CLASSPATH_ENV   = CLASSPATH=$(SHARED_OBJECTS):${top_builddir}:$(JAVAROOT):$(srcdir)/$(JAVAROOT):${top_srcdir}/jjtraveler-1.3.jar:$$CLASSPATH

EXTRA_DIST = test.trm test.taf

check_JAVA      = Test1.java \
		  Test2.java \
		  TestFib.java \
		  TestFibInterpreted.java \
		  TestPrimes.java \
		  VisitorBenchmark.java

TESTS = $(foreach t, ${check_JAVA}, $t.test)

%.java.test : %.java Makefile
	@(echo '#! /bin/sh';\
	 echo 'set -e';\
	 echo 'CLASSPATH=${top_builddir}:..:${SHARED_OBJECTS}:${top_srcdir}/jjtraveler-1.3.jar:${CLASSPATH}';\
	 echo 'export CLASSPATH';\
	 echo "java test.$* ${srcdir}";\
	 echo "rm $*.java.test";\
	) > $@ && chmod +x $@

CLEANFILES+=*.test
