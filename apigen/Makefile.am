SUBDIRS		= apigen lib utils doc

EXTRA_DIST	= make_rules
DISTFILE	= ${distdir}.tar.gz
WWWDIR		= /hosts/www/projects/MetaEnv/unreleased

www-dist: ${DISTFILE}
	cp ${DISTFILE} ${WWWDIR}
	chgrp sen ${WWWDIR}/${DISTFILE}
	chmod g+w ${WWWDIR}/${DISTFILE}
