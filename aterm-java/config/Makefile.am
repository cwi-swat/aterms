bin_SCRIPTS = aterm-java-config

EXTRA_DIST = aterm-java-config.sh.src

aterm-java-config: aterm-java-config.sh.src
	sed 's@__PREFIX__@${prefix}@g' < $< > $@ && chmod +x $@
