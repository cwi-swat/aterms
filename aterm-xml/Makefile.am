SUBDIRS	= config aterm2xml xml2aterm test

ACLOCAL_FLAGS = -I .

EXTRA_DIST      = $(wildcard *.pkg) package 

noinst_SCRIPTS = ${PACKAGE}-${VERSION}.pkg

${PACKAGE}-${VERSION}.pkg: package Makefile
	cp $< $@

CLEANFILES = ${PACKAGE}-${VERSION}.pkg
