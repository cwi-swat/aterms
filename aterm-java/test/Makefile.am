javadir         = ${prefix}/test
JAVAROOT        = ${top_builddir}/test
CLASSPATH_ENV   = CLASSPATH=$(SHARED_OBJECTS):${top_builddir}:$(JAVAROOT):$(srcdir)/$(JAVAROOT):$$CLASSPATH

java_JAVA       = Test1.java \
		  TestFib.java \
		  TestFibInterpreted.java \
		  TestPrimes.java

TESTS = $(foreach t, ${java_JAVA}, $t.test)

%.java.test : %.java Makefile
	@(echo '#! /bin/sh';\
	 echo 'set -e';\
	 echo 'export CLASSPATH=..:${SHARED_OBJECTS}:${CLASSPATH}';\
	 echo "java $*";\
	 echo "rm $*.java.test";\
	) > $@ && chmod +x $@
