#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "_aterm.h"

/*{{{  void termsize(FILE *file) */

void termsize(FILE *file)
{
  int core_size, text_size, term_depth;

  ATerm t = ATreadFromFile(file);
  core_size = AT_calcCoreSize(t);
  text_size = AT_calcTextSize(t);
  term_depth = AT_calcTermDepth(t);
  printf("internal size: %d bytes, text size: %d bytes, depth: %d\n", 
	 core_size, text_size, term_depth);
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  int i;
  ATerm bottomOfStack;

  for(i=1; i<argc; i++) {
    if(strcmp(argv[i], "-h") == 0) {
      ATwarning("usage: %s [aterm-options] < input > output\n", argv[0]);
      ATwarning("  use -at-help to get aterm-options\n");
      exit(0);
    }
  }

  ATinit(argc, argv, &bottomOfStack);
  termsize(stdin);

  return 0;
}

/*}}}  */
