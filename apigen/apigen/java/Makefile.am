#
# vim:ts=8:sw=8:sts=0
#
# $Id$
#

java_JAVA       = JavaGen.java

javadir         = ${prefix}/classes/apigen/java

JAVAROOT        = ${top_builddir}

GENERATED	= PhoneNumber.java Rolodex.java

test: all
	gen-java-test -basedir ${JAVAROOT} -package apigen.java \
		-input rolodex.adt -import "java.util.*" -verbose
	javac ${GENERATED}

test-sdf: all
	gen-java-test -package sdf -input SDF.adt -verbose
	javac -classpath .:${CLASSPATH} -d . sdf/*.java
