SUBDIRS = config .

ACLOCAL_FLAGS = -I .

JARFILE = shared-objects-${VERSION}.jar
JARFILE_ENV = JAR=${JARFILE}

data_DATA = ${JARFILE}
noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

EXTRA_DIST = package build.xml ${JARFILE}

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $< $@

CLEANFILES = ${PACKAGE}-${VERSION}.pkg

if MAINTAINER_MODE
${JARFILE}:
	${JARFILE_ENV} ant dist

clean-local:
	${JARFILE_ENV} ant clean
endif
