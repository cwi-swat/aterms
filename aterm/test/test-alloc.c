
/**
  * Test the node allocation functions
  */

#include <stdio.h>
#include <assert.h>
#include "memory.h"
#include "symbol.h"

void
testAlloc(void)
{
  int i;
  ATerm *test;

  fprintf(stderr, "Allocating 18 nodes of size 3:\n");
  for(i=0; i<18; i++) {
    test = AT_allocate(3);
    if(test)
      fprintf(stderr, "Result: %p\n", test);
    else
      fprintf(stderr, "allocation failed.\n");
  }

  printf("test succeeded.\n");
}

void
testSymbol(void)
{
	int i;
	Symbol *symmies[16];

	symmies[0] = ATmakeSymbol("aanroep", 3, ATfalse);
	symmies[1] = ATmakeSymbol("aanroep", 3, ATtrue);
	symmies[2] = ATmakeSymbol("aanroep", 4, ATfalse);
	symmies[3] = ATmakeSymbol("aanroep", 4, ATtrue);
	symmies[4] = ATmakeSymbol("aanroep", 3, ATfalse);

	assert(symmies[0] == symmies[4]);
	for (i=0; i<5; i++)
	{
		fprintf(stdout, "symmies[%d]: ", i);
		AT_printSymbol(symmies[i], stdout);
	}
	assert(symmies[1] != symmies[2]);
	assert(symmies[1] != symmies[3]);
	assert(symmies[2] != symmies[3]);
}

int main(int argc, char *argv[])
{
  int bottomOfStack;

  ATinit(argc, argv, NULL, &bottomOfStack);

  testAlloc();
  testSymbol();

  return 0;
}
