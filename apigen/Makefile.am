SUBDIRS		= apigen lib utils test doc

DISTFILE	= ${distdir}.tar.gz
WWWDIR		= /hosts/www/projects/MetaEnv/apigen

www-dist: ${DISTFILE}
	cp ${DISTFILE} ${WWWDIR}
	chgrp sen ${WWWDIR}/${DISTFILE}
	chmod g+w ${WWWDIR}/${DISTFILE}

EXTRA_DIST=make_rules $(wildcard *.pkg)

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: ${PACKAGE}.pkg Makefile
	sed 's#__VERSION__#${VERSION}#g;\
             s#__PACKAGE__#${PACKAGE}#g' < $< > $@

