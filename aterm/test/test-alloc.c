
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
  ATerm test;

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
	Symbol symmies[8];

	symmies[0] = ATmakeSymbol("application", 3, ATfalse);
	symmies[1] = ATmakeSymbol("application", 3, ATtrue);
	symmies[2] = ATmakeSymbol("application", 4, ATfalse);
	symmies[3] = ATmakeSymbol("application", 4, ATtrue);
	symmies[4] = ATmakeSymbol("application", 3, ATfalse);

	assert(symmies[0] == symmies[4]);
	for (i=0; i<5; i++)
	{
		fprintf(stdout, "symmies[%d]: ", i);
		AT_printSymbol(symmies[i], stdout);
		fprintf(stdout, "\n");
	}
	assert(symmies[1] != symmies[2]);
	assert(symmies[1] != symmies[3]);
	assert(symmies[2] != symmies[3]);
}

void
testAppl(void)
{
  int i;
  Symbol symmies[4];
  ATermAppl apples[16];

  symmies[0] = ATmakeSymbol("f0", 0, ATfalse);
  symmies[1] = ATmakeSymbol("f1", 1, ATfalse);
  symmies[2] = ATmakeSymbol("f6", 6, ATfalse);

  apples[0] = ATmakeAppl0(symmies[0]);
  apples[1] = ATmakeAppl1(symmies[1], (ATerm)apples[0]);
  apples[2] = ATmakeAppl1(symmies[1], (ATerm)apples[1]);
  apples[3] = ATmakeAppl1(symmies[1], (ATerm)apples[0]);
  apples[4] = ATmakeAppl6(symmies[2], (ATerm)apples[0], (ATerm)apples[0], 
			  (ATerm)apples[1], (ATerm)apples[0], 
			  (ATerm)apples[0], (ATerm)apples[1]);

  assert(apples[1] == apples[3]);
  assert(apples[1] != apples[2]);
  assert(apples[2] != apples[3]);
  assert(apples[0] != apples[1]);

  for(i=0; i<5; i++) {
    fprintf(stdout, "apples[%d] = ", i);
    ATwriteToTextFile((ATerm)apples[i], stdout);
    fprintf(stdout, "\n");
  }
}

int main(int argc, char *argv[])
{
  int bottomOfStack;

  ATinit(argc, argv, NULL, &bottomOfStack);

  testAlloc();
  testSymbol();
  testAppl();

  return 0;
}
