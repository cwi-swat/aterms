#
# $Id$
#
# vim:ts=8:sw=8

include ../make_rules

CFLAGS			= -Wall -g -O2
EXTRA_DIST		= $(bin_SCRIPTS) trm2taf.src

INCLUDES		= -I$(top_srcdir)/aterm

LDFLAGS			= -L$(top_builddir)/aterm/

LDLIBS			= -lATerm

bin_PROGRAMS		= termsize baffle dicttoc atsum atdiff atrmannos

termsize_SOURCES	= termsize.c
termsize_LDADD		= $(LDLIBS)

baffle_SOURCES		= baffle.c
baffle_LDADD		= $(LDLIBS)

dicttoc_SOURCES		= dicttoc.c
dicttoc_LDADD		= $(LDLIBS)

atsum_SOURCES		= atsum.c
atsum_LDADD		= $(LDLIBS)

atdiff_SOURCES		= atdiff.c
atdiff_LDADD		= $(LDLIBS)

atrmannos_SOURCES       = atrmannos.c
atrmannos_LDADD         =  $(LDLIBS)

bin_SCRIPTS		= baf2trm baf2taf trm2baf trm2taf taf2trm taf2baf \
			  trmcat

%: %.src
	sed 's@__BUILDDIR__@$(top_builddir)@g;' < $< > $@ && chmod +x $@

