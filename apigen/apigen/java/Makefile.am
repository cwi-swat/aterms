# $Id$

CLASSPATH_ENV	= CLASSPATH=${top_srcdir}:$(ATERM)/classes

java_JAVA       = JavaGen.java

javadir         = ${prefix}/classes/apigen/java

JAVAROOT        = ${top_builddir}

GENERATED	= PhoneNumber.java Rolodex.java

EXTRA_DIST	= rolodex.adt SDF.adt

test: all
	gen-java-test -basedir ${JAVAROOT} -package apigen.java \
		-input rolodex.adt -verbose
	javac ${GENERATED}

test-sdf: all
	mkdir -p sdf
	gen-java-test -package sdf -input SDF.adt -verbose
	javac -classpath .:${CLASSPATH} -d . sdf/*.java
