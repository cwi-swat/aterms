SUBDIRS		= aterm utils test $(JAVA_SUBDIR) doc

DIST_SUBDIRS	= aterm utils test java doc
EXTRA_DIST	= make_rules LICENSE $(noinst_DATA)

noinst_DATA	= aterm.pkg

DISTFILE	= ${distdir}.tar.gz
WWWDIR		= /hosts/www/projects/MetaEnv/aterm

aterm.pkg: configure.in ${srcdir}/utils/genpkg.sh
	${srcdir}/utils/genpkg.sh > $@

www-dist: distcheck
	install --mode=0664 --group=sen ${DISTFILE} ${WWWDIR}
	gzip --best < doc/at.ps > doc/at.ps.gz
	install --mode=0664 --group=sen doc/at.ps.gz ${WWWDIR}/doc
	gzip --best < doc/manual.ps > doc/manual.ps.gz
	install --mode=0664 --group=sen doc/manual.ps.gz ${WWWDIR}/doc
	install --mode=0664 --group=sen java/aterm-javadoc.tar.gz ${WWWDIR}/doc
	cd ${WWWDIR}; make
