
CLEANFILES = ${PACKAGE}-config

bin_SCRIPTS = ${PACKAGE}-config

${PACKAGE}-config: Makefile
	@(	echo "#! /bin/sh";\
		echo "echo ${prefix}/lib" \
	) > $@ && chmod +x $@
