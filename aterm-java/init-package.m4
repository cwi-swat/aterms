
AC_DEFUN(GLT_INIT_PACKAGE,
[
 AM_INIT_AUTOMAKE(esyscmd([grep "name\s*=\s*[0-9a-zA-Z\.\-\_]*" package | sed -e 's/.*name\s*=\s*//g']),esyscmd([grep "version\s*=\s*[0-9a-zA-Z\.]*" package | sed -e 's/.*version\s*=\s*//g']))
])
