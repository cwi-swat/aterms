
/**
  * Test the node allocation functions
  */

#include "memory.h"
#include <stdio.h>

int main(int argc, char *argv[])
{
  int i, bottomOfStack;
  ATerm *test;

  Tinit(argc, argv, NULL, &bottomOfStack);

  fprintf(stderr, "Allocating 18 nodes of size 3:\n");
  for(i=0; i<18; i++) {
    test = T_allocate(3);
    if(test)
      fprintf(stderr, "Result: %p\n", test);
    else
      fprintf(stderr, "allocation failed.\n");
  }

  printf("test succeeded.\n");

  return 0;
}
