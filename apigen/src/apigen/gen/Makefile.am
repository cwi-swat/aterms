# $Id: Makefile.am,v 1.7 2001/07/16 13:41:54 jong Exp $

SUBDIRS = . tom c java 

CLASSPATH_ENV = CLASSPATH=${top_srcdir}:$(EXTERNAL_JARS)
JAVAROOT = ${top_builddir}

javadir = ${prefix}/classes/apigen/gen
java_JAVA = \
	GenerationParameters.java \
	Generator.java \
	StringConversions.java \
	TypeConversions.java \
	TypeConverter.java
