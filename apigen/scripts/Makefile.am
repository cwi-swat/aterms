# $Id$

bin_SCRIPTS = adt-to-c adt-to-java adt-to-tom
noinst_SCRIPTS = adt-to-c-test adt-to-java-test

EXTRA_DIST = $(wildcard *.src)

ABS_TOP_BUILDDIR=$(shell cd ${top_builddir}; pwd)

%: %.src Makefile
	sed -e 's#__JAVA_ATERM__#${JAVA_ATERM}#g' \
		-e 's#__APIGEN_CLASSES__#${prefix}/classes#g' \
		-e 's#__DICT_TO_C__#${ATERM}/bin/dicttoc#g' < $< > $@ \
	&& chmod +x $@

%-test: %.src Makefile
	sed -e 's#__JAVA_ATERM__#${JAVA_ATERM}#g' \
		-e 's#__APIGEN_CLASSES__#${ABS_TOP_BUILDDIR}#g' \
		-e 's#__DICT_TO_C__#${ATERM}/bin/dicttoc#g' < $< > $@ \
	&& chmod +x $@

CLEANFILES = ${bin_SCRIPTS} ${noinst_SCRIPTS}
