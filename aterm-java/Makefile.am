SUBDIRS = aterm test config .

JJTRAVELER = jjtraveler-1.3.jar

EXTRA_DIST = ${JJTRAVELER}

noinst_DATA = ${JAVADOCDIR}.tar.gz
data_DATA = ${JARFILE} ${JJTRAVELER}

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
		-classpath ${top_builddir}:${SHARED_OBJECTS}:${top_srcdir}/jjtraveler-1.3.jar \
		-d ${JAVADOCDIR} \
		$(JAVA_FILES)
	tar cf - ${JAVADOCDIR} | gzip --best > ${JAVADOCDIR}.tar.gz
	@rm -rf ${JAVADOCDIR}

