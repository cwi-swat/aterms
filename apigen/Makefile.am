SUBDIRS = apigen scripts lib-1.0 lib-2.0 utils test doc

JARFILE    = apigen-${VERSION}.jar
DISTFILE = ${distdir}.tar.gz
data_DATA  = ${JARFILE}
CLEANFILES = ${JARFILE}

${JARFILE}:
	find ${top_builddir}/apigen -name '*.class' | xargs jar cf ${JARFILE}

WWWDIR = /hosts/www/projects/MetaEnv/apigen

www-dist: ${DISTFILE}
	cp ${DISTFILE} ${WWWDIR}
	chgrp sen ${WWWDIR}/${DISTFILE}
	chmod g+w ${WWWDIR}/${DISTFILE}

EXTRA_DIST = $(wildcard *.pkg)

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: ${PACKAGE}.pkg Makefile
	sed 's#__VERSION__#${VERSION}#g;\
             s#__PACKAGE__#${PACKAGE}#g' < $< > $@

