

bin_SCRIPTS = ${PACKAGE}-config

CLEANFILES = ${PACKAGE}-config

${PACKAGE}-config: Makefile
	@(	echo "#! /bin/sh";\
		echo "echo ${prefix}/lib" \
	) > $@ && chmod +x $@
