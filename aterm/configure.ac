# $Id$

m4_pattern_forbid([^META_])

AC_INIT
AC_CANONICAL_TARGET([])
META_SETUP

AC_AIX
AC_PROG_CC
AC_LIBTOOL_WIN32_DLL
AC_PROG_LIBTOOL

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
])
AC_OUTPUT
