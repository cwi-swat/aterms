javadir		= ${prefix}/classes/aterm/test

java_JAVA	= Test.java

JAVAROOT	= ${top_builddir}/java/

TESTS		= aterm-test.sh

EXTRA_DIST	= aterm-test.sh.src test.trm

CLEANFILES	= test.trm2 test.taf test.taf2

%: %.src
	sed 's@__BUILDDIR__@$(top_builddir)@g;' < $< > $@ && chmod +x $@
