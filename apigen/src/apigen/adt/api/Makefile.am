# $Id: Makefile.am,v 1.13 2003/06/27 09:13:16 markvdb Exp $

include $(top_srcdir)/Makefile.meta

api:
	adt-to-java \
		--input ${top_srcdir}/lib-3.0/ADT.adt \
		--package apigen.adt \
		--name api \
		--verbose \
		--output ../../..
