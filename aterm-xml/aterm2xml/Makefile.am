bin_PROGRAMS = aterm2xml
lib_LIBRARIES = libaterm2xml.a

libaterm2xml_a_SOURCES=aterm2xml.c

noinst_HEADERS    = aterm2xml.h
aterm2xml_SOURCES = 
aterm2xml_LDADD   = -L. -laterm2xml -L${ATERM}/lib -lATerm 

INCLUDES          = -I${ATERM}/include -I${top_srcdir}/config
