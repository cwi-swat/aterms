libexec_PROGRAMS = XML2ATERM
bin_SCRIPTS      = xml2aterm

noinst_HEADERS    = xml2aterm.h
XML2ATERM_SOURCES = xml2aterm.c
XML2ATERM_LDADD   = -L${ATERM}/lib -lATerm ${XMLLIB}
INCLUDES          = -I${ATERM}/include -I${top_srcdir}/config ${XMLINCLUDE}

CLEANFILES += ${bin_SCRIPTS}

% : %.sh.src Makefile
	sed 's@__XMLLIBPATH__@${XML}/lib@g;\
             s@__PREFIX__@${prefix}@g' < $< > $@ && chmod +x $@
