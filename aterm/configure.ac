# $Id$

AC_INIT
AC_CONFIG_SRCDIR([configure])
AC_PREREQ(2.59)
AC_CANONICAL_TARGET([])

GLT_INIT_PACKAGE

AM_MAINTAINER_MODE

# ATERM_ left in configure is the sign a macro was not defined, or
# there was a typo in a macro invocation.
m4_pattern_forbid([^ATERM_])

# Remember if CFLAGS were set, to override the default of AC_PROG_CC.
ATERM_INIT_CFLAGS

AC_AIX
AC_PROG_CC
AC_LIBTOOL_WIN32_DLL
AC_PROG_LIBTOOL

# If CFLAGS was not set, initialize to -O3 if gcc is used
ATERM_OPTIMIZE_CFLAGS

# Set to -Wall if gcc is used
ATERM_WARNING_CFLAGS

# Add a configuration option to allow users to control sharing.
ATERM_WITH_SHARING

CURDATE=`date`
AC_SUBST([CURDATE])

AC_CHECK_FUNCS([strdup])
AC_CHECK_FUNCS([lrand48 srand48])

AC_CONFIG_FILES([Makefile
  aterm/Makefile
  utils/Makefile
  test/Makefile
  doc/Makefile
  doc/examples/Makefile
  doc/spec/Makefile
  aterm.spec
  aterm.pc
])
AC_OUTPUT
