
/**
  * Test the node allocation functions
  */

/*{{{  includes */

#include <stdio.h>
#include <assert.h>
#include "memory.h"
#include "symbol.h"

/*}}}  */
/*{{{  defines */

#define test_assert(cat,id,cond) if(!cond) test_failed(cat, id)

/*}}}  */

/*{{{  void test_failed(char *category, int id) */

/**
  * A test has failed.
  */

void test_failed(char *category, int id)
{
  fprintf(stderr, "%s test %d failed!\n", category, id);
  exit(1);
}

/*}}}  */
/*{{{  void test_term(char *cat, int id, ATerm t, int type) */

/**
  * Check the result of a test
  */

void test_term(char *cat, int id, ATerm t, int type)
{
  if(!t)
    test_failed(cat, id);
  /*ATverify(t);*/
  if(type != -1) {
    if(ATgetType(t) != type)
      test_failed(cat, id);
  }
}

/*}}}  */

/*{{{  void testAlloc(void) */

/**
  * Test basic allocation function(s)
  */

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

/*}}}  */
/*{{{  void testSymbol(void) */

/**
  * Test symbol creation and printing
  */

void
testSymbol(void)
{
	int i;
	Symbol symmies[8];

	symmies[0] = ATmakeSymbol("application", 3, ATfalse);
	symmies[1] = ATmakeSymbol("application", 3, ATtrue);
	symmies[2] = ATmakeSymbol("An \" \n \r \t \\ application", 4, ATtrue);
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

/*}}}  */
/*{{{  void testOther(void) */

/**
  * Test term creation of other term types
  */

void
testOther(void)
{
	ATermInt  aint[8];
	ATermReal real[8];
	ATermPlaceholder ph[8];
	ATermBlob blob[8];
	char data[10] = "123456789";

	aint[0] = ATmakeInt(1234);
	real[0] = ATmakeReal((double)1.2345678);

	fprintf(stdout, "aint[%d] = ", 0);
	ATwriteToTextFile((ATerm)aint[0], stdout);
	fprintf(stdout, "\n");

	fprintf(stdout, "real[%d] = ", 0);
	ATwriteToTextFile((ATerm)real[0], stdout);
	fprintf(stdout, "\n");

	ph[0] = ATmakePlaceholder((ATerm)ATmakeAppl0(ATmakeSymbol("int",0,ATfalse)));
	ATwriteToTextFile((ATerm)ph[0], stdout);
	fprintf(stdout, "\n");

	blob[0] = ATmakeBlob((void *)data, 9);
	assert(ATgetBlobSize(blob[0]) == 9);
	assert(ATgetBlobData(blob[0]) == data);
	ATwriteToTextFile((ATerm)blob[0], stdout);
	fprintf(stdout, "\n");
}

/*}}}  */
/*{{{  void testAppl(void) */

/**
  * Test creation of function applications
  */

void
testAppl(void)
{
  int i;
  Symbol symmies[4];
  ATermAppl apples[16];

  symmies[0] = ATmakeSymbol("f0", 0, ATfalse);
  symmies[1] = ATmakeSymbol("f1", 1, ATfalse);
  symmies[2] = ATmakeSymbol("f6", 6, ATfalse);
  symmies[3] = ATmakeSymbol("f10", 10, ATfalse);

  apples[0] = ATmakeAppl0(symmies[0]);
  apples[1] = ATmakeAppl1(symmies[1], (ATerm)apples[0]);
  apples[2] = ATmakeAppl1(symmies[1], (ATerm)apples[1]);
  apples[3] = ATmakeAppl1(symmies[1], (ATerm)apples[0]);
  apples[4] = ATmakeAppl6(symmies[2], (ATerm)apples[0], (ATerm)apples[0], 
			  (ATerm)apples[1], (ATerm)apples[0], 
			  (ATerm)apples[0], (ATerm)apples[1]);
  apples[5] = ATmakeAppl(symmies[3], apples[0], apples[1], apples[0],
			 apples[1], apples[0], apples[1], apples[0],
			 apples[1], apples[0], apples[1]);

  assert(apples[1] == apples[3]);
  assert(apples[1] != apples[2]);
  assert(apples[2] != apples[3]);
  assert(apples[0] != apples[1]);

  for(i=0; i<6; i++) {
    fprintf(stdout, "apples[%d] = ", i);
    ATwriteToTextFile((ATerm)apples[i], stdout);
    fprintf(stdout, "\n");
  }
}

/*}}}  */
/*{{{  void testList(void) */

/**
  * Test list operations.
  */

void testList(void)
{
  int i;
  ATermList list[16];

  list[0] = ATmakeList0();
  list[1] = ATmakeList1((ATerm)ATmakeInt(1));
  list[2] = ATmakeList2((ATerm)ATmakeInt(1), (ATerm)ATmakeInt(2));
  list[3] = ATmakeList3((ATerm)ATmakeInt(1), (ATerm)ATmakeInt(2), 
			(ATerm)ATmakeInt(3));
  list[4] = ATmakeList4((ATerm)ATmakeInt(1), (ATerm)ATmakeInt(2), 
			(ATerm)ATmakeInt(3), (ATerm)ATmakeInt(4));
  list[5] = ATmakeList5((ATerm)ATmakeInt(1), (ATerm)ATmakeInt(2), 
			(ATerm)ATmakeInt(3), (ATerm)ATmakeInt(4), 
			(ATerm)ATmakeInt(5));
  list[6] = ATmakeList6((ATerm)ATmakeInt(1), (ATerm)ATmakeInt(2), 
			(ATerm)ATmakeInt(3), (ATerm)ATmakeInt(4), 
			(ATerm)ATmakeInt(5), (ATerm)ATmakeInt(6));
  list[7] = ATmakeList3((ATerm)ATmakeInt(1), (ATerm)ATmakeInt(2), 
			(ATerm)ATmakeInt(3));
  list[8] = ATmakeList2((ATerm)ATmakeInt(2), (ATerm)ATmakeInt(3));

  assert(ATisEmpty(list[0]));
  assert(!ATisEmpty(list[1]));

  for(i=0; i<6; i++) {
    test_term("list-creation", i+1, (ATerm)list[i], AT_LIST);
    assert(ATgetLength(list[i]) == i);
    ATwriteToTextFile((ATerm)list[i], stdout);
    fprintf(stdout, "\n");
  }

  test_assert("list-ops", 1, ATisEqual(list[3], ATgetPrefix(list[4])));
  test_assert("list-ops", 2, ATisEqual(ATmakeInt(6), ATgetLast(list[6])));
  test_assert("list-ops", 3, ATisEqual(list[8], ATgetSlice(list[5], 1, 3)));
  test_assert("list-ops", 4, ATisEmpty(ATgetSlice(list[6], 0, 0)));
  test_assert("list-ops", 5, ATisEmpty(ATgetSlice(list[7], 2, 2)));
  test_assert("list-ops", 6, ATisEmpty(ATgetSlice(list[7], 1, 1)));
  test_assert("list-ops", 7, ATisEqual(list[2], ATgetSlice(list[2],0,2)));
  test_assert("list-ops", 8, ATisEqual(ATgetFirst(list[5]), ATmakeInt(1)));
  test_assert("list-ops", 9, ATisEqual(ATgetNext(list[3]), list[8]));
  test_assert("list-ops",10, ATisEqual(ATinsert(list[8], 
						(ATerm)ATmakeInt(1)), list[3]));
  test_assert("list-ops",11, ATisEqual(ATappend(list[3], 
						(ATerm)ATmakeInt(4)), list[4]));
  list[15] = ATconcat(list[4], list[3]);
  test_assert("list-ops",12, ATgetLength(list[15]) == 7);
  list[14] = ATconcat(list[3], list[3]);
  test_assert("list-ops",13, ATgetLength(list[14]) == 6);
  list[13] = ATinsertAt(list[14], (ATerm)ATmakeInt(4), 3);
  test_assert("list-ops",14, ATisEqual(list[13], list[15]));

  test_assert("list-ops",15, ATisEqual(ATelementAt(list[4], 1),
				       (ATerm)ATmakeInt(2)));
  test_assert("list-ops",16, ATindexOf(list[4], (ATerm)ATmakeInt(2),0) == 1);
  test_assert("list-ops",17, ATlastIndexOf(list[4], (ATerm)ATmakeInt(2),0) == 1);
  test_assert("list-ops",16, ATindexOf(list[4], (ATerm)ATmakeInt(2),2) == -1);
  test_assert("list-ops",17, ATlastIndexOf(list[4], (ATerm)ATmakeInt(2),0) == 1);

  test_assert("list-ops",18, ATisEqual(ATgetArguments(ATmakeAppl(ATmakeSymbol("f",2,0),
								 (ATerm)ATmakeInt(1),
								 (ATerm)ATmakeInt(2))),
				       list[2]));

}

/*}}}  */
/*{{{  void testRead(void) */

/**
  * Test read functions
  */

void
testRead(void)
{
  ATerm t;
  FILE *f = fopen("test.trms", "r");
  if(!f)
    ATerror("cannot open file \"test.trms\"");

  do {
    t = ATreadFromTextFile(f);
    if(t) {
	  ATprintf("term read: %t\n", t);
    } else
      fprintf(stdout, "no more terms to read.\n");
  } while(t);

  fclose(f);

  t = ATreadFromString("f(1)");
  fprintf(stdout, "read from string: ");
  ATwriteToTextFile(t, stdout);
  t = ATreadFromString("f(a,b,<123>,0.456,\"f\")");
  fprintf(stdout, "\nread from string: ");
  ATwriteToTextFile(t, stdout);
  fprintf(stdout, "\nread from string: ");
  t = ATreadFromString("f(00000004:1234,xyz,[1,2,3])");
  ATwriteToTextFile(t, stdout);
  fprintf(stdout, "\n");
}

/*}}}  */
/*{{{  void testMake(void) */

void
testMake(void)
{
}

/*}}}  */
/*{{{  void testMatch(void) */

/**
  * Test matching functions
  */

void testMatch(void)
{
}

/*}}}  */
/*{{{  void testPrintf(void) */
void testPrintf()
{
/*	ATfprintf(stderr, "Test: %3.4d\n", 3);*/
}
/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

/**
  * Test some features of the aterm library.
  */

int main(int argc, char *argv[])
{
  int bottomOfStack;

  ATinit(argc, argv, NULL, &bottomOfStack);

  testAlloc();
  testSymbol();
  testAppl();
  testList();
  testOther();
  testRead();
  testPrintf();

  return 0;
}

/*}}}  */


