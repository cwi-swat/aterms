SUBDIRS = shared .

JARFILE = shared-objects-${VERSION}.jar

data_DATA = ${JARFILE}
CLEANFILES = ${JARFILE}

${JARFILE}:
	jar cf ${JARFILE} \
	`cd ${top_builddir} && find . -name '*.java' -o -name '*.class'`

