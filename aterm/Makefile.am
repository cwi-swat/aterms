SUBDIRS = aterm utils test doc

CLEANFILES = ${PACKAGE}-${VERSION}.pkg

DIST_SUBDIRS = aterm utils test doc

EXTRA_DIST = make_rules LICENSE $(wildcard *.pkg) aterm.spec

DISTFILE = ${distdir}.tar.gz
WWWDIR = /hosts/www/projects/MetaEnv/aterm

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: ${PACKAGE}.pkg Makefile
	sed 's#__VERSION__#${VERSION}#g;\
             s#__PACKAGE__#${PACKAGE}#g' < $< > $@

www-dist: distcheck
	install --mode=0664 --group=sen ${DISTFILE} ${WWWDIR}
	gzip --best < doc/at.ps > doc/at.ps.gz
	install --mode=0664 --group=sen doc/at.ps.gz ${WWWDIR}/doc
	gzip --best < doc/manual.ps > doc/manual.ps.gz
	install --mode=0664 --group=sen doc/manual.ps.gz ${WWWDIR}/doc
	cd ${WWWDIR}; make
