SUBDIRS	= config aterm2xml xml2aterm test

ACLOCAL_FLAGS = -I .

EXTRA_DIST      = $(wildcard *.pkg)

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: ${PACKAGE}.pkg Makefile
	sed 's#__VERSION__#${VERSION}#g;\
             s#__PACKAGE__#${PACKAGE}#g' < $< > $@

CLEANFILES = ${PACKAGE}-${VERSION}.pkg
