# $Id$

SUBDIRS = entry separator module modulename

EXTRA_DIST = ${java_JAVA}

JAVAROOT = ${top_builddir}
javadir = ${prefix}/classes/apigen/adt/api/types

CLASSPATH_ENV=CLASSPATH=${JAVAROOT}:${EXTERNAL_JARS}

java_JAVA = Entries.java  Entry.java \
						Separator.java  Separators.java \
						Imports.java Module.java Modules.java \
						ModuleName.java
