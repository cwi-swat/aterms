SUBDIRS = aterm test config .

noinst_DATA = ${JAVADOCDIR}.tar.gz
data_DATA = ${JARFILE} 

JARFILE = aterm-${VERSION}.jar
JAVADOCDIR = aterm-javadoc
CLEANFILES = ${JARFILE} ${JAVADOC}.tar.gz

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

${PACKAGE}-${VERSION}.pkg: ${PACKAGE}.pkg Makefile
	sed 's#__VERSION__#${VERSION}#g;\
             s#__PACKAGE__#${PACKAGE}#g' < $< > $@
