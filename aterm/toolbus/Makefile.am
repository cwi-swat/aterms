# $Id$
#
# vim:ts=8:sw=8
include ../make_rules

bin_PROGRAMS		= tifstoc tblog

LDFLAGS			= -L${top_builddir}/aterm/ -L.
LDLIBS			= -lATB -lATerm

tifstoc_SOURCES		= tifstoc.c atb-tool.h
tifstoc_LDADD		= libATB.a -L${top_builddir}/aterm/ -lATerm

tblog_SOURCES		= tblog.c atb-tool.h
tblog_LDADD		= tblog_dict.o logger.tif.o ${LDLIBS}

INCLUDES		= -I$(top_srcdir)/aterm

# The following libraries are compiled to static libraries.
lib_LIBRARIES		= libATB.a @TOOLBUS_LIB_GCC@ @TOOLBUS_LIB_CC@ @TOOLBUS_LIB_DBG@ \
			 @TOOLBUS_LIB_PROF@ @TOOLBUS_LIB_SHARED@
EXTRA_LIBRARIES		= libATB-gcc.a libATB-cc.a libATB-dbg.a libATB-prof.a

ALLSRCS			= atb-tool.c

PUBLIC_INCL		= atb-tool.h

PRIVATE_INCL		=

ALL_INCLUDES		= ${PUBLIC_INCL} ${PRIVATE_INCL}

include_HEADERS		= $(PUBLIC_INCL)

EXTRA_DIST		= $(ALLSRCS) tblog-interface.tb tblog-interface.tifs\
			 tblog.dict

CLEANFILES		= logger.tif.c logger.tif.h tblog_dict.c tblog_dict.h

# static only libraries
libATB_a_SOURCES	=
libATB_cc_a_SOURCES	=
libATB_gcc_a_SOURCES	=
libATB_dbg_a_SOURCES	=
libATB_prof_a_SOURCES	=

libATB_a_LIBADD		= $(ALLSRCS:.c=.o)
libATB_cc_a_LIBADD	= $(ALLSRCS:.c=-cc.o)
libATB_gcc_a_LIBADD	= $(ALLSRCS:.c=-gcc.o)
libATB_dbg_a_LIBADD	= $(ALLSRCS:.c=-dbg.o)
libATB_prof_a_LIBADD	= $(ALLSRCS:.c=-prof.o)

logger.tif.c logger.tif.h : tblog-interface.tifs
	PATH=$(top_builddir)/toolbus/:$$PATH tifstoc -tool logger $<

logger.tif.o: logger.tif.c logger.tif.h

tblog_dict.o: tblog_dict.c tblog_dict.h
tblog.o: tblog_dict.h logger.tif.h

%_dict.c %_dict.h: %.dict ${top_builddir}/utils/dicttoc
	${top_builddir}/utils/dicttoc -dict $<

# The output of $(TOOLBUS) is moved to $@ to make sure the generated .tifs
# file exists in the current directory. The ToolBus by default uses the
# dirname of its second argument to determine the location where to store
# its output. This does not work correct when using VPATH in the Makefile.
tblog-interface.tifs: tblog-interface.tb
	$(TOOLBUS) -gentifs $<
	@if [ "a${VPATH}" != "a" ]; then \
	  mv ${srcdir}/tblog-interface.tifs $@ ;\
	fi
