bin_PROGRAMS = xml2aterm

noinst_HEADERS    = xml2aterm.h
xml2aterm_SOURCES = xml2aterm.c
xml2aterm_LDADD   = -L${ATERM}/lib -lATerm ${XMLLIB}
INCLUDES          = -I${ATERM}/include -I${top_srcdir}/config ${XMLINCLUDE}
