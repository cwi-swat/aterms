bin_SCRIPTS = aterm-java-config

EXTRA_DIST = aterm-java-config.sh.src program-check.m4

aterm-java-config: aterm-java-config.sh.src
	sed 's@__PREFIX__@${prefix}@g;\
	     s@__EXTERNAL_JARS__@${EXTERNAL_JARS}@g;\
             s@__VERSION__@${VERSION}@g' < $< > $@ && chmod +x $@
