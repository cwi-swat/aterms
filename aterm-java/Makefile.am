# $Id$

SUBDIRS = aterm test config .

ACLOCAL_FLAGS = -I .

EXTRA_DIST = package

noinst_DATA = ${JAVADOCDIR}.tar.gz
data_DATA = ${JARFILE} 

JARFILE = aterm-${VERSION}.jar
JAVADOCDIR = aterm-javadoc
CLEANFILES = ${JARFILE} ${JAVADOCDIR}.tar.gz $(wildcard aterm-java-*.pkg)

JAVA_CLASSES = \
	AFun \
	ATerm \
	ATermAppl \
	ATermBlob \
	ATermFactory \
	ATermInt \
	ATermList \
	ATermPlaceholder \
	ATermReal


javadoc: ${JAVADOCDIR}.tar.gz

${JARFILE}:
	jar cf ${JARFILE} \
	`cd ${top_builddir} && find . -name '*.class'`

JAVA_FILES = $(patsubst %,$(srcdir)/aterm/%.java,$(JAVA_CLASSES)) \
	$(srcdir)/aterm/pure/PureFactory.java

${JAVADOCDIR}.tar.gz: ${JAVA_FILES}
	@mkdir -p ${JAVADOCDIR}
	javadoc -public \
		-classpath ${top_builddir}:${EXTERNAL_JARS} \
		-d ${JAVADOCDIR} \
		$(JAVA_FILES)
	tar cf - ${JAVADOCDIR} | gzip --best > ${JAVADOCDIR}.tar.gz
	@rm -rf ${JAVADOCDIR}

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $ < $@

