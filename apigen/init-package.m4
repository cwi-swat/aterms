
AC_DEFUN([GLT_INIT_PACKAGE],
[
 AM_INIT_AUTOMAKE(esyscmd([grep "name[:blank:]*=.*" package | cut -f2 -d= | tr -d '[:blank:]']),esyscmd([grep "version[:blank:]*=.*" package | cut -f2 -d= | tr -d '[:blank:]']))
])
