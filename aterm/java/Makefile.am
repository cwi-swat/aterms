SUBDIRS = aterm

JARFILE = aterm-${VERSION}.jar

noinst_DATA = ${JAVADOCDIR}.tar.gz
data_DATA = ${JARFILE}

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

JAVA_FILES = $(patsubst %,$(srcdir)/aterm/%.java,$(JAVA_CLASSES)) \
	$(srcdir)/aterm/pure/PureFactory.java

JAVADOCDIR = aterm-javadoc

javadoc: ${JAVADOCDIR}.tar.gz

${JAVADOCDIR}.tar.gz: ${JAVA_FILES}
	@mkdir -p ${JAVADOCDIR}
	javadoc -public -d ${JAVADOCDIR} $(JAVA_FILES)
	tar cf - ${JAVADOCDIR} | gzip --best > ${JAVADOCDIR}.tar.gz
	@rm -rf ${JAVADOCDIR}

${JARFILE}:
	jar cf ${JARFILE} \
	`cd ${top_builddir}/java && find . -name '*.java'` \
	`cd ${top_builddir}/java && find . -name '*.class'`
