SUBDIRS=aterm utils toolbus test $(JAVA_SUBDIR) doc

DIST_SUBDIRS=aterm utils toolbus test doc java

EXTRA_DIST=make_rules reconf

DISTFILE	= ${distdir}.tar.gz
WWWDIR		= /usr/local/www.cwi.nl/projects/MetaEnv/aterm

www-dist: distcheck
	cp ${DISTFILE} ${WWWDIR}
	gzip < doc/at.ps > ${WWWDIR}/doc/at.ps.gz
	gzip < doc/manual.ps > ${WWWDIR}/doc/manual.ps.gz
	cp java/aterm-javadoc.tar.gz ${WWWDIR}/doc
	cd ${WWWDIR}; make
