bin_SCRIPTS = apigen-config

EXTRA_DIST = apigen-config.sh.src

apigen-config: apigen-config.sh.src Makefile
	sed 's@__PREFIX__@${prefix}@g;\
	     s@__EXTERNAL_JARS__@${EXTERNAL_JARS}@g;\
             s@__VERSION__@${VERSION}@g' < $< > $@ && chmod +x $@

CLEANFILES = $(bin_SCRIPTS)

