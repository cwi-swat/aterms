
#include <stdio.h>
#include <atb-tool.h>
/*#include "test.tif.c"*/

void rec_terminate(int fd, ATerm *t)
{
  ATprintf("rec_terminate called: %d, %t\n", fd, t);
}

void just_a_test(int fd)
{
  ATprintf("just_a_test called: %d\n", fd);
}

ATermAppl test_handler(int fd, ATermAppl term)
{
  ATprintf("test_handler (%d): %t\n", fd, term);
  return NULL;
}

ATermAppl test_check_in_sign(int fd, ATermAppl sign)
{
  ATprintf("test_check_in_sign (%d): %t\n", fd, sign);
  return NULL;
}



int main(int argc, char *argv[])
{
  ATerm topOfStack = NULL;

  ATBinit(argc, argv, &topOfStack);
  ATBconnect(NULL, NULL, -1, test_handler, test_check_in_sign);
  return ATBeventloop();
}

