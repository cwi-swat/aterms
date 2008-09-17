dnl $Id$

AC_INIT
META_SETUP
META_C_SETUP
AC_PROG_LIBTOOL

dnl Require Xml library
AC_PACKAGE_REQUIRE(libxml2, xml2-config,
   [  --with-libxml2=DIR          XML library is in DIR])
XML="${LIBXML2}"
XMLCFG="${XML}/bin/xml2-config"
dnl To be able to bundle this package with the sources of libxml2, we
dnl delay calling xml2-config until build time using the single quotes
dnl around the backticks.
XMLLIB='`${XMLCFG} --libs`'
XMLINCLUDE='`${XMLCFG} --cflags`/libxml `${XMLCFG} --cflags`'
AC_SUBST(LIBXML2)
AC_SUBST(XML)
AC_SUBST(XMLLIB)
AC_SUBST(XMLCFG)
AC_SUBST(XMLINCLUDE)

AC_OUTPUT
