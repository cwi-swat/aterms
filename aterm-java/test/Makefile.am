javadir         = ${top_builddir}/test/classfiles
JAVAROOT        = ${top_builddir}
CLASSPATH_ENV   = CLASSPATH=${top_builddir}:$(JAVAROOT):$(srcdir)/$(JAVAROOT):${EXTERNAL_JARS}

EXTRA_DIST = test.trm test.taf $(JAVASOURCES)


JAVASOURCES     = Test1.java \
		  Test2.java \
		  TestFib.java \
		  TestFibInterpreted.java \
		  TestPrimes.java \
		  VisitorBenchmark.java

check_JAVA = $(JAVASOURCES)

TESTS = $(foreach t, ${check_JAVA}, $t.test)

%.java.test : %.java Makefile
	@(echo '#! /bin/sh';\
	 echo 'set -e';\
	 echo 'CLASSPATH=${top_builddir}:..:${EXTERNAL_JARS}:${CLASSPATH}';\
	 echo 'export CLASSPATH';\
	 echo "java test.$* ${srcdir}";\
	 echo "rm $*.java.test";\
	) > $@ && chmod +x $@

CLEANFILES = *.test test.taf2 test.trm2
