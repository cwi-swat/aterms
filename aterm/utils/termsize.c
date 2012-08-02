#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "_aterm.h"
#include "memory.h"

extern void AT_statistics();

/*{{{  void termsize(FILE *file) */

void termsize(FILE *file)
{
  unsigned long core_size, text_size, term_depth, unique_syms;
  unsigned long unique_terms, allocated_bytes;

  ATerm t = ATreadFromFile(file);

  core_size = AT_calcCoreSize(t);
  text_size = AT_calcTextSize(t);
  term_depth = AT_calcTermDepth(t);
  unique_syms = ATcalcUniqueSymbols(t);
  unique_terms = ATcalcUniqueSubterms(t);
  allocated_bytes = AT_calcAllocatedSize();

  printf("internal size   : %ld bytes\n"
	 "text size       : %ld bytes\n"
	 "depth           : %ld\n"
	 "unique symbols  : %ld\n"
	 "unique terms    : %ld\n"
	 "allocated bytes : %ld\n",
	 core_size, text_size, term_depth, unique_syms,
	 unique_terms, allocated_bytes);
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
