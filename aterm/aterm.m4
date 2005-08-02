
# ATERM_WITH_SHARING
# ------------------
AC_DEFUN([ATERM_WITH_SHARING], [
  AC_MSG_CHECKING([whether term sharing is enabled])
  AC_ARG_WITH(
    [sharing],
    [AS_HELP_STRING([--with-sharing],[create libraries that do term sharing @<:@yes@:>@])],
    [if test "$withval" = "no"; then
       AC_MSG_RESULT([no])
       AC_DEFINE([NO_SHARING])
       AC_DEFINE([WITH_STATS])
     else
       if test "$withval" != "yes"; then
         AC_MSG_RESULT([unknown value specified for --with-sharing.])
         AC_MSG_ERROR([Please use --without-sharing, --with-sharing, or --with-sharing=yes|no])
       else
         AC_MSG_RESULT([yes])
       fi
     fi
    ],
    [AC_MSG_RESULT([yes])]
  )
])

# ATERM_INIT_CFLAGS
# -----------------
# Initialize CFLAGS and remember if the CFLAGS was set by the user or by us.
AC_DEFUN([ATERM_INIT_CFLAGS], [
  AC_MSG_CHECKING([whether CFLAGS is already set])
  if test "${CFLAGS+set}" = set; then
    AC_MSG_RESULT([yes])
    aterm_cflags_set=yes
  else
    AC_MSG_RESULT([no])
    aterm_cflags_set=no

    # Avoid setting of CFLAGS by AC_PROG_CC.
    CFLAGS=""
  fi
])

# ATERM_WARNINGCFLAGS
# -------------------
# Sets WARNINGCFLAGS to -Wall if GCC is used.
AC_DEFUN([ATERM_WARNING_CFLAGS], [
  AC_REQUIRE([AC_PROG_CC])

  if test "$GCC" = "yes"; then
    WARNINGCFLAGS=-Wall
  else
    WARNINGCFLAGS=
  fi

  AC_SUBST([WARNINGCFLAGS])
])

# ATERM_OPTIMIZE_CFLAGS
# ----------------
# For gcc, set CFLAGS to -O3 if CFLAGS was not set.
AC_DEFUN([ATERM_OPTIMIZE_CFLAGS], [
  # if CFLAGS was not set by the user
  if test "$aterm_cflags_set" = no; then
    # and GCC is used (other compilers don't accept -Ox)
    if test "$GCC" = "yes"; then
      OPTIMIZECFLAGS="-O3"
    fi
  fi

  AC_SUBST([OPTIMIZECFLAGS])
])

# XT_SVN_REVISION
# ---------------
AC_DEFUN([XT_SVN_REVISION],
[
AC_MSG_CHECKING([for the SVN revision of the source tree])

if test -e ".svn"; then
   REVFIELD="1"
   SVN_REVISION=`svn status -v -N -q ./ | awk "{ if(\\\$NF == \".\") print \\\$$REVFIELD }"`
   AC_MSG_RESULT($SVN_REVISION)
else
  if test -e "svn-revision"; then
    SVN_REVISION="`cat svn-revision`"
    AC_MSG_RESULT($SVN_REVISION)
  else
    SVN_REVISION="0"
    AC_MSG_RESULT([not available, defaulting to 0])
  fi
fi
AC_SUBST([SVN_REVISION])

])

# XT_PRE_RELEASE
# ---------------
AC_DEFUN([XT_PRE_RELEASE],
[
  AC_REQUIRE([XT_SVN_REVISION])
  VERSION="${VERSION}pre${SVN_REVISION}"
  PACKAGE_VERSION="${PACKAGE_VERSION}pre${SVN_REVISION}"
])
