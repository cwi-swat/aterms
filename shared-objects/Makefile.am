SUBDIRS = shared config .

ACLOCAL_FLAGS = -I .

JARFILE = shared-objects-${VERSION}.jar

data_DATA = ${JARFILE}
CLEANFILES = ${JARFILE} ${PACKAGE}-${VERSION}.pkg

${JARFILE}:
	jar cf ${JARFILE} \
	`cd ${top_builddir} && find . -name '*.class'`

EXTRA_DIST=package

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $< $@


