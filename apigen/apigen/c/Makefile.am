#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

java_JAVA       = CGen.java

javadir         = ${prefix}/classes/apigen/c

JAVAROOT        = ${top_builddir}

test: all
	gen-c-test -prefix RL -prologue rolodex.pro -input rolodex.adt
