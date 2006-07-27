if MAINTAINER_MODE

EXTRA_DIST = test.trm test.taf 


JAVASOURCES     = Test1 \
		  Test2 \
		  TestFib \
		  TestFibInterpreted \
		  TestPrimes \
		  VisitorBenchmark

TESTS = $(foreach t, ${JAVASOURCES}, $t.test)

%.test : Makefile
	@(echo '#! /bin/sh';\
	 echo 'set -e';\
	 echo 'CLASSPATH=${top_builddir}/${JAVA_JAR}:${EXTERNAL_JARS}';\
	 echo 'export CLASSPATH';\
	 echo "java aterm.test.$* ${srcdir}"; \
	) > $@ && chmod +x $@

CLEANFILES = *.test test.taf2 test.trm2

endif
