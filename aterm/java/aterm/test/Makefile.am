javadir		= ${prefix}/classes/aterm/test

java_JAVA	= Test.java VisitorBenchmark.java

noinst_SCRIPTS	= aterm-test.sh

JAVAROOT	= ${top_builddir}/java/

TESTS		= aterm-test.sh

EXTRA_DIST	= aterm-test.sh.src test.trm

CLEANFILES	= test.trm2 test.taf test.taf2

bench: all
	java aterm.test.VisitorBenchmark 27 2
	java -cp VisitorBenchmark.zip aterm.test.VisitorBenchmark 27 2

%: %.src
	sed 's@__BUILDDIR__@$(top_builddir)@g;\
             s@__SRCDIR__@$(srcdir)@g' < $< > $@ && chmod +x $@
