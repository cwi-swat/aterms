# $Id: Makefile.am,v 1.13 2003/06/27 09:13:16 markvdb Exp $

include ADTMakeRules.am

EXTRA_DIST = ADTMakeRules.am

JAVAROOT = ${top_builddir}
javadir = ${prefix}/classes/apigen/adt/api

CLASSPATH_ENV=CLASSPATH=${JAVAROOT}:${EXTERNAL_JARS}

java_JAVA = ${ADTAPI}

api:
	adt-to-java \
		--input ${top_srcdir}/lib-2.0/ADT.adt \
		--folding \
		--package apigen.adt.api \
		--name ADT \
		--import 'aterm.*' \
		--verbose
	mv ${srcdir}/ADTMakeRules ${srcdir}/ADTMakeRules.am
