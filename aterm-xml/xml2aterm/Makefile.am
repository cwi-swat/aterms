libexec_PROGRAMS = XMLTOATERM
bin_SCRIPTS      = xml2aterm

EXTRA_DIST = xml2aterm.sh.src

noinst_HEADERS    = xml2aterm.h
XMLTOATERM_SOURCES = xml2aterm.c
XMLTOATERM_LDADD   = -L${ATERM}/lib -lATerm ${XMLLIB}
INCLUDES          = -I${ATERM}/include -I${top_srcdir}/config ${XMLINCLUDE}

CLEANFILES += ${bin_SCRIPTS}

% : %.sh.src Makefile
	sed 's@__XMLLIBPATH__@${XML}/lib@g;\
             s@__PREFIX__@${prefix}@g' < $< > $@ && chmod +x $@
