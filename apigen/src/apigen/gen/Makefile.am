#
# vim:ts=8:sw=8:sts=0
#
# $Id: Makefile.am,v 1.7 2001/07/16 13:41:54 jong Exp $
#

SUBDIRS = . tom c java 

CLASSPATH_ENV = CLASSPATH=${top_srcdir}:$(EXTERNAL_JARS)


javadir = ${prefix}/classes/apigen/gen
JAVAROOT = ${top_builddir}

java_JAVA = \
	GenerationParameters.java \
	Generator.java \
	StringConversions.java \
	TypeConversions.java \
	TypeConverter.java
