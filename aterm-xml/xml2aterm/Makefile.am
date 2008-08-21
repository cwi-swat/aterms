libexec_PROGRAMS = XMLTOATERM
bin_SCRIPTS      = xml2aterm
noinst_SCRIPTS   = xml2aterm.test

EXTRA_DIST = xml2aterm.sh.src

noinst_HEADERS    = xml2aterm.h
XMLTOATERM_SOURCES = xml2aterm.c
XMLTOATERM_LDADD   = -L${ATERM}/lib -lATerm ${XMLLIB}
INCLUDES          = -I${ATERM}/include -I${top_srcdir}/config ${XMLINCLUDE}

CLEANFILES = ${bin_SCRIPTS} ${noinst_SCRIPTS}

% : %.sh.src Makefile
	sed 's@__XMLLIBPATH__@${XML}/lib@g;s@__XMLTOATERMBIN__@${prefix}/libexec@g' < $< > $@ && chmod +x $@

%.test: %.sh.src Makefile
	sed 's@__XMLLIBPATH__@${XML}/lib@g;s@__XMLTOATERMBIN__@${top_builddir}/xml2aterm@g' < $< > $@ && chmod +x $@
