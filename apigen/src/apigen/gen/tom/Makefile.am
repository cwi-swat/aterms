# $Id: Makefile.am,v 1.7 2001/07/16 13:41:54 jong Exp $

SUBDIRS = c java

CLASSPATH_ENV = CLASSPATH=${top_srcdir}:$(EXTERNAL_JARS)

javadir = ${prefix}/classes/apigen/gen/tom
JAVAROOT = ${top_builddir}

java_JAVA = \
	TomGenerationParameters.java \
	TomSignatureGenerator.java \
	TomSignatureImplementation.java \
	TomTypeConversions.java
