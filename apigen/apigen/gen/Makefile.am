#
# vim:ts=8:sw=8:sts=0
#
# $Id: Makefile.am,v 1.7 2001/07/16 13:41:54 jong Exp $
#

SUBDIRS = . c java 

CLASSPATH_ENV	= CLASSPATH=${top_srcdir}:$(JAVA_ATERM):$(SHARED_OBJECTS)

java_JAVA       = Generator.java TomSignatureGenerator.java \
                  StringConversions.java TomSignatureImplementation.java \
                  TypeConverter.java

javadir         = ${prefix}/classes/apigen/gen
JAVAROOT	= ${top_builddir}
