SUBDIRS = apigen scripts lib-1.0 lib-2.0 utils test doc config

ACLOCAL_FLAGS = -I .

JARFILE    = apigen-${VERSION}.jar
DISTFILE = ${distdir}.tar.gz
data_DATA  = ${JARFILE}
CLEANFILES = ${JARFILE} ${PACKAGE}-*.pkg

${JARFILE}:
	find ${top_builddir}/apigen \
		-name '*.class' \
		-o -name '*.java' \
	| xargs jar cf ${JARFILE}

WWWDIR = /hosts/www/projects/MetaEnv/apigen

www-dist: ${DISTFILE}
	cp ${DISTFILE} ${WWWDIR}
	chgrp sen ${WWWDIR}/${DISTFILE}
	chmod g+w ${WWWDIR}/${DISTFILE}

EXTRA_DIST = package

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $< $@

