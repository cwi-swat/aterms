SUBDIRS = shared config .

JARFILE = shared-objects-${VERSION}.jar

data_DATA = ${JARFILE}
CLEANFILES = ${JARFILE} ${PACKAGE}-${VERSION}.pkg

${JARFILE}:
	jar cf ${JARFILE} \
	`cd ${top_builddir} && find . -name '*.class'`

EXTRA_DIST=$(wildcard *.pkg)

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: ${PACKAGE}.pkg Makefile
	sed 's#__VERSION__#${VERSION}#g;\
             s#__PACKAGE__#${PACKAGE}#g' < $< > $@

