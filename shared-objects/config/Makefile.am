bin_SCRIPTS = shared-objects-config

EXTRA_DIST = shared-objects-config.sh.src

shared-objects-config: shared-objects-config.sh.src
	sed 's@__PREFIX__@${prefix}@g' < $< > $@ && chmod +x $@
