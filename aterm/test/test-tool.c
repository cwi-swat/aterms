#include <stdio.h>
#include <atb-tool.h>

/*{{{  void rec_terminate(int fd, ATerm t) */

void rec_terminate(int fd, ATerm t)
{
  ATprintf("rec_terminate called: %d, %t\n", fd, t);
}

/*}}}  */
/*{{{  void just_a_test(int fd) */

void just_a_test(int fd)
{
  ATprintf("just_a_test called: %d\n", fd);
}

/*}}}  */
/*{{{  ATerm test_handler(int fd, ATerm term) */

ATerm test_handler(int fd, ATerm term)
{
  ATprintf("test_handler (%d): %t\n", fd, term);
  return NULL;
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  ATerm topOfStack = NULL;

  ATBinit(argc, argv, &topOfStack);
  ATBconnect(NULL, NULL, -1, test_handler);
  return ATBeventloop();
}

/*}}}  */

