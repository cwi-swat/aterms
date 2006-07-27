
include $(top_srcdir)/Makefile.java.meta

SUBDIRS = . scripts lib utils test
EXTRA_DIST += $(JAVA_LOCAL_JARS)

ACLOCAL_AMFLAGS = -I .

api:
	adt-to-java \
		--input $(top_srcdir)/lib/ADT.adt \
		--package apigen.adt \
		--name api \
		--verbose \
		--output ./src
