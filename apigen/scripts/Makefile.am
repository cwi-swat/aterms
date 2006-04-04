# $Id$

bin_SCRIPTS = adt-to-c adt-to-java adt-to-tom
noinst_SCRIPTS = adt-to-c-test adt-to-java-test

EXTRA_DIST = $(wildcard *.src)

ABS_TOP_SRCDIR=$(shell cd ${top_srcdir}; pwd)

%: %.src Makefile
	sed -e 's#__JAVA_ATERM__#${JAVA_ATERM}#g' \
		-e 's#__APIGEN_CLASSES__#${prefix}/share/apigen/apigen.jar#g' \
		-e 's#__DICT_TO_C__#${ATERM}/bin/dicttoc#g' < $< > $@ \
	&& chmod +x $@

%-test: %.src Makefile
	sed -e 's#__JAVA_ATERM__#${JAVA_ATERM}#g' \
		-e 's#__APIGEN_CLASSES__#${ABS_TOP_SRCDIR}/apigen.jar#g' \
		-e 's#__DICT_TO_C__#${ATERM}/bin/dicttoc#g' < $< > $@ \
	&& chmod +x $@

CLEANFILES = ${bin_SCRIPTS} ${noinst_SCRIPTS}
