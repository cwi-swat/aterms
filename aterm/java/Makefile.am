SUBDIRS		= aterm

noinst_DATA	= ${JAVADOCDIR}.tar.gz

JAVA_CLASSES	= AFun ATerm ATermInt ATermReal ATermBlob ATermAppl \
		ATermList ATermPlaceholder ATermFactory
JAVA_FILES	= $(patsubst %,$(srcdir)/aterm/%.java,$(JAVA_CLASSES)) \
		$(srcdir)/aterm/pure/PureFactory.java

JAVADOCDIR	= aterm-javadoc

javadoc: ${JAVADOCDIR}.tar.gz

${JAVADOCDIR}.tar.gz: ${JAVA_FILES}
	@mkdir -p ${JAVADOCDIR}
	javadoc -public -d ${JAVADOCDIR} $(JAVA_FILES)
	tar cf - ${JAVADOCDIR} | gzip --best > ${JAVADOCDIR}.tar.gz
	@rm -rf ${JAVADOCDIR}
