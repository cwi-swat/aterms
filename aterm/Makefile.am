SUBDIRS		= aterm utils test $(JAVA_SUBDIR) doc

DIST_SUBDIRS	= aterm utils test java doc
EXTRA_DIST	= make_rules reconf $(noinst_DATA)

noinst_DATA	= aterm.pkg

DISTFILE	= ${distdir}.tar.gz
WWWDIR		= /hosts/www/projects/MetaEnv/aterm

aterm.pkg:
	${srcdir}/utils/genpkg.sh > $@

www-dist: distcheck
	cp ${DISTFILE} ${WWWDIR}
	chmod g+w ${WWWDIR}/${DISTFILE}
	gzip --best < doc/at.ps > ${WWWDIR}/doc/at.ps.gz
	chmod g+w ${WWWDIR}/doc/at.ps.gz
	gzip --best < doc/manual.ps > ${WWWDIR}/doc/manual.ps.gz
	chmod g+w ${WWWDIR}/doc/manual.ps.gz
	cp java/aterm-javadoc.tar.gz ${WWWDIR}/doc
	chmod g+w ${WWWDIR}/doc/aterm-javadoc.tar.gz
	cd ${WWWDIR}; make
