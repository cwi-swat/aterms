SUBDIRS = aterm toolbus

JAVA_CLASSES	= AFun ATerm ATermInt ATermReal ATermBlob ATermAppl \
		ATermList ATermPlaceholder ATermFactory
JAVA_FILES	= $(patsubst %,aterm/%.java,$(JAVA_CLASSES))

JAVADOCDIR	= aterm-javadoc

javadoc: ${JAVA_FILES}
	@mkdir -p ${JAVADOCDIR}
	javadoc -public -d ${JAVADOCDIR} $(JAVA_FILES)
	tar cf - ${JAVADOCDIR} | gzip > ${JAVADOCDIR}.tar.gz

