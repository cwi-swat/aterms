# $Id$

CLASSPATH_ENV	= CLASSPATH=${top_srcdir}:$(ATERM)/classes

java_JAVA       = CGen.java

javadir         = ${prefix}/classes/apigen/c

JAVAROOT	= ${top_builddir}

test: all
	gen-c-test -prefix RL -prologue rolodex.pro -input rolodex.adt
