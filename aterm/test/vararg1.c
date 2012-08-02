#include <stdio.h>
#include <stdarg.h>

/*{{{  void va_argtest(va_list *args) */

void va_argtest(va_list *args)
{
  printf("arg: %s\n", va_arg(*args, char *));
}

/*}}}  */
/*{{{  void va_listtest(int nr, va_list *args) */

void va_listtest(int nr, va_list *args)
{
  int i;

  for(i=0; i<nr; i++) {
    va_argtest(args);
  }
  printf("%d arguments.\n", nr);
}

/*}}}  */
/*{{{  void va_test(int nr, ...) */

void va_test(int nr, ...)
{
  va_list args;

  va_start(args, nr);
  va_listtest(nr, &args);
  va_end(args);
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  va_test(4,"a","b", "c","d");
  return 0;
}

/*}}}  */
