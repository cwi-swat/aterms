
include $(top_srcdir)/Makefile.java.meta

SUBDIRS = . scripts lib-1.0 lib-2.0 lib-3.0 utils test config

ACLOCAL_AMFLAGS = -I .

api:
	adt-to-java \
		--input ${top_srcdir}/lib-3.0/ADT.adt \
		--package apigen.adt \
		--name api \
		--verbose \
		--output ./src
