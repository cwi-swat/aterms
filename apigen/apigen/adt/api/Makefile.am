# $Id: Makefile.am,v 1.13 2003/06/27 09:13:16 markvdb Exp $

SUBDIRS = types

EXTRA_DIST = ${java_JAVA}

JAVAROOT = ${top_builddir}
javadir = ${prefix}/classes/apigen/adt/api

CLASSPATH_ENV=CLASSPATH=${JAVAROOT}:${top_srcdir}:${EXTERNAL_JARS}

java_JAVA = \
	AbstractType.java \
	Factory.java

api:
	adt-to-java \
		--input ${top_srcdir}/lib-3.0/ADT.adt \
		--package apigen.adt \
		--name api \
		--verbose \
		--nojar \
		--output ../../..
