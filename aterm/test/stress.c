
/**
  * Test the node allocation functions
  */

/*{{{  includes */

#include <stdio.h>
#include <assert.h>
#include "_aterm.h"
#include "memory.h"
#include "asymbol.h"
#include "util.h"
#include "gc.h"

/*}}}  */
/*{{{  defines */

#define test_assert(cat,id,cond) if(!(cond)) test_failed(cat, id)

/*}}}  */
/*{{{  globals */

char stress_id[] = "$Id$";

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
		ATprintf("symmies[%d]: %y\n", i, symmies[i]);
	assert(symmies[1] != symmies[2]);
	assert(symmies[1] != symmies[3]);
	assert(symmies[2] != symmies[3]);

	for (i=0; i< 3*65535/2; i++)
	{
		char buf[BUFSIZ];
		sprintf(buf, "xxx%d", i);
		ATmakeSymbol(buf, 0, ATtrue);
	}

}

/*}}}  */
/*{{{  void testOther(void) */

/**
  * Test term creation of other term types
  */

void
testOther(void)
{
	ATerm t[4];
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

	blob[0] = ATmakeBlob(9, (void *)data);
	assert(ATgetBlobSize(blob[0]) == 9);
	assert(ATgetBlobData(blob[0]) == data);
	ATwriteToTextFile((ATerm)blob[0], stdout);
	fprintf(stdout, "\n");

	test_assert("text-size", 1,  AT_calcTextSize(ATparse("[]")) == 2);

	t[0] = ATparse("f(1,[2,3],<[a,b]>,1.243,g(h(i(a,a),a),a,a))");
	t[1] = ATparse("1");
	t[2] = ATparse("f(1,2)");
	ATprintf("AT_calcCoreSize(%t) = %d\n", t[0], AT_calcCoreSize(t[0]));
	ATprintf("AT_calcCoreSize(%t) = %d\n", t[1], AT_calcCoreSize(t[1]));
	ATprintf("AT_calcCoreSize(%t) = %d\n", t[2], AT_calcCoreSize(t[2]));
}

/*}}}  */
/*{{{  void testAppl(void) */

/**
  * Test creation of function applications
  */

void
testAppl(void)
{
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
  apples[6] = ATsetArgument(apples[2], (ATerm)apples[0], 0);

  assert(apples[6] == apples[1]);
  assert(apples[1] == apples[3]);
  assert(apples[2] != apples[1]);
  assert(apples[2] != apples[6]);
  assert(apples[1] != apples[2]);
  assert(apples[2] != apples[3]);
  assert(apples[0] != apples[1]);

  ATprintf("application tests ok.\n");
}

/*}}}  */
/*{{{  void testList(void) */

/**
  * Test list operations.
  */

ATbool lower3(ATerm t)
{
	if(ATgetInt((ATermInt)t) < 3)
		return ATtrue;
	return ATfalse;
}

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

	ATprintf("list nodes: %n, %n, %n, %n\n", list[0], list[1], list[2], list[3]);

  for(i=0; i<6; i++) {
    test_term("list-creation", i+1, (ATerm)list[i], AT_LIST);
    assert(ATgetLength(list[i]) == i);
    /*ATwriteToTextFile((ATerm)list[i], stdout);
    fprintf(stdout, "\n");*/
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
  test_assert("list-ops",17, ATlastIndexOf(list[4], 
					   (ATerm)ATmakeInt(2), -1) == 1);
  test_assert("list-ops",16, ATindexOf(list[4], (ATerm)ATmakeInt(2),2) == -1);
  test_assert("list-ops",17, ATlastIndexOf(list[4], 
					   (ATerm)ATmakeInt(2),0) == -1);

  test_assert("list-ops",18, ATisEqual(ATgetArguments(ATmakeAppl(ATmakeSymbol("f",2,0),
								 (ATerm)ATmakeInt(1),
								 (ATerm)ATmakeInt(2))),
				       list[2]));

  list[10] = (ATermList)ATreadFromString("[1,2,3,4,5]");
  list[11] = ATreplace(list[10], (ATerm)ATmakeInt(0), 2);

  test_assert("list-ops", 19, ATisEqual(list[11], 
				  ATreadFromString("[1, 2, 0, 4, 5]")));

  for(i=0; i<5; i++)
	list[11] = ATreplace(list[11], (ATerm)ATmakeInt(0), i);

  test_assert("list-ops", 20, ATisEqual(list[11], 
				  ATreadFromString(" [0,0,0,0,0] ")));

	ATfprintf(stderr, "result of ATremoveElement: %t\n", 
						ATremoveElement((ATermList)ATparse("[1,2,3,2]"), ATparse("2")));
	test_assert("list-ops", 21, 
							ATisEqual(ATremoveElement((ATermList)ATparse("[1,2,3,2]"),
																				ATparse("2")), ATparse("[1,3,2]")));
	test_assert("list-ops", 22, 
							ATisEqual(ATremoveAll((ATermList)ATparse("[1,2,3,2]"),
																		ATparse("2")), ATparse("[1,3]")));

	test_assert("list-ops", 23,
							ATisEqual(ATfilter((ATermList)ATparse("[1,2,3,4,5,6,5,4,3,2,1]"),
																 lower3), ATparse("[1,2,2,1]")));

  printf("list tests ok.\n");
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
  } while(t && !ATisEqual(t, ATparse("\"the end\"")));

  fclose(f);

  t = ATreadFromString("f(1)");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("f(a,b,<123>,0.456,\"f\")");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("f(00000004:1234,xyz,[1,2,3])");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("[]");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("f{[a,1],[b,ab{[1,2]}]}");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("<int>");
  ATfprintf(stdout, "read from string: %t\n", t);
	t = ATreadFromString("\"quoted: \\\"abc\\\"\"");
  ATfprintf(stdout, "read from string: %t\n", t);

  fprintf(stdout, "Next term should give a parse error at line 0, col 17\n");
  f = fopen("error.trm", "r");
  t = ATreadFromTextFile(f);
  fclose(f);

}

/*}}}  */
/*{{{  void testDict(void) */

/**
  * Testing dictionaries
  */

void testDict(void)
{
  ATerm key[4];
  ATerm value[4];
  ATerm dict[4];

  key[0] = ATreadFromString("key-0");
  key[1] = ATreadFromString("key-1");
  key[2] = ATreadFromString("key-2");
  key[3] = ATreadFromString("key-3");

  value[0] = ATreadFromString("val-0");
  value[1] = ATreadFromString("val-1");
  value[2] = ATreadFromString("val-2");
  value[3] = ATreadFromString("val-3");
  value[0] = ATreadFromString("val-0");

  dict[0] = ATdictPut(ATdictCreate(), key[0], value[0]);
  dict[1] = ATdictPut(dict[0], key[1], value[1]);
  dict[2] = ATdictPut(dict[1], key[2], value[2]);
  dict[3] = ATdictPut(dict[2], key[3], value[3]);
  
  test_assert("dict", 1, ATdictGet(ATdictCreate(), key[0]) == NULL);
  test_assert("dict", 2, ATisEqual(ATdictGet(dict[1], key[0]), value[0]));
  test_assert("dict", 3, ATisEqual(ATdictGet(dict[2], key[1]), value[1]));
  test_assert("dict", 4, ATisEqual(dict[2], ATdictRemove(dict[3], key[3])));

  printf("dictionary tests ok.\n");
}

/*}}}  */
/*{{{  void testMake(void) */

void
testMake(void)
{
	int len = 8;
	char data[8] = "ABCDEFG";
	Symbol sym[8];

	test_assert("make", 1, ATisEqual(ATmake("<int>", 3), ATmakeInt(3)));
	test_assert("make", 2, ATisEqual(ATmake("<real>", 3.8), ATmakeReal(3.8)));
	test_assert("make", 3, ATisEqual(ATmake("<blob>", len, data),
									 ATmakeBlob(len, data)));
	sym[0] = ATmakeSymbol("abc", 0, ATfalse);
	test_assert("make", 4, ATisEqual(ATmake("<appl>", "abc"),
									 ATmakeAppl0(sym[0])));
	sym[1] = ATmakeSymbol("def", 0, ATtrue);
	test_assert("make", 5, ATisEqual(ATmake("<str>", "def"),
									 ATmakeAppl0(sym[1])));
	sym[2] = ATmakeSymbol("echt", 3, ATfalse);

	test_assert("make", 6, ATisEqual(ATmake("<appl(<int>, <list>)>",
		"echt", 123, ATmakeList2((ATerm)ATmakeInt(7), (ATerm)ATmakeReal(7.01))),
		ATreadFromString("echt(123, 7, 7.01)")));
	test_assert("make", 7, ATisEqual(ATmake("<placeholder>", ATmakeInt(7)),
		ATmakePlaceholder((ATerm)ATmakeInt(7))));

	test_assert("make", 8, ATisEqual(ATmake("w(<str>)", " "),
					 ATparse("w(\" \")")));

	ATprintf("ATmake with 9 args: %t\n",
			 ATmake("f(<int>,<int>,<int>,<int>,<int>,"
					"<int>,<int>,<int>,<int>)", 1, 2, 3, 4, 5, 6, 7, 8, 9));
 
	ATprintf("ATmakeAppl: %t\n",
				ATmakeAppl(ATmakeSymbol("f", 9, ATfalse), ATmakeInt(1),
                           ATmakeInt(2),ATmakeInt(3),ATmakeInt(4),
                           ATmakeInt(5),ATmakeInt(6),ATmakeInt(7),
						   ATmakeInt(8),ATmakeInt(9)));

	test_assert("make", 9, ATisEqual(ATmake("f(<int>,<int>,<int>,<int>,<int>,"
											"<int>,<int>,<int>,<int>)", 
											1, 2, 3, 4, 5, 6, 7, 8, 9), 
				ATmakeAppl(ATmakeSymbol("f", 9, ATfalse), ATmakeInt(1),
                           ATmakeInt(2),ATmakeInt(3),ATmakeInt(4),
                           ATmakeInt(5),ATmakeInt(6),ATmakeInt(7),
						   ATmakeInt(8),ATmakeInt(9))));

	test_assert("make", 10, ATisEqual(ATmake("[\"f\"([<list>])]", 
																					 ATparse("[1,2,3]")),
																		ATparse("[\"f\"([1,2,3])]")));

	fprintf(stderr, "The following tests should generate parse errors.\n");
	ATparse("<int");
	ATparse("f(<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>asdfaksdjfhasjkhf)");

	printf("make tests ok.\n");
}

/*}}}  */
/*{{{  void testMatch(void) */

/**
  * Test matching functions
  */

void testMatch(void)
{
  ATerm t[8];
  int i;
  double r;
  ATerm type;
  char *name[2];
  int size;
  void *data;
  ATermList list;

  t[0] = ATmake("f(1,3.14,<placeholder>,a,\"b\",00000004:abcd)", 
				ATmake("type"));
  t[1] = ATmake("[1,2,3]");
	t[2] = ATmake("f(1,2,3)");

  test_assert("match", 1, ATmatch(ATmake("1"), "<int>", &i));
  test_assert("match", 2, i == 1);
  test_assert("match", 3, ATmatch(ATmake("3.14"), "<real>", &r));
  test_assert("match", 4, r == 3.14);
  
  test_assert("match", 11, ATmatch(t[0], "f(<int>,<real>,<placeholder>,"
								  "<appl>,<str>,<blob>)",
              &i, &r, &type, &name[0], &name[1], &size, &data));
  test_assert("match", 12, i == 1);
  test_assert("match", 13, r == 3.14);
  test_assert("match", 14, ATisEqual(type, ATmake("type")));
  test_assert("match", 15, streq(name[0], "a"));
  test_assert("match", 16, streq(name[1], "b"));
  test_assert("match", 17, size == 4);
  test_assert("match", 18, ((char *)data)[1] == 'b');

			  
	test_assert("match", 19, ATmatch(t[0], "<appl(1,<real>,<term>,<id>,"
								 "<appl(<list>)>,<term>)>",
              &name[0], &r, &t[7], &name[1], &name[2], &list, &t[6]));
  test_assert("match", 20, r == 3.14);
  test_assert("match", 21, streq(name[0], "f"));
  test_assert("match", 22, streq(name[2], "b"));
  test_assert("match", 23, ATisEqual(ATreadFromString("<type>"), t[7]));
  test_assert("match", 24, size == 4);
  test_assert("match", 25, ((char *)data)[1] == 'b');

  test_assert("match", 26, ATmatch(t[1], "[1,<list>]", &list));
  test_assert("match", 27, ATisEqual(ATmake("[2,3]"), list));

  test_assert("match", 28, !ATmatch(ATmake("f"), "<str>", &name[0]));
  test_assert("match", 29, !ATmatch(ATmake("\"f\""), "<id>", &name[0]));
  test_assert("match", 30, !ATmatch(ATmake("f"), "<appl(1)>", &name[0]));
  test_assert("match", 31, !ATmatch(ATmake("f(1)"), "<appl>", &name[0]));
	test_assert("match", 32, ATmatch(t[2], "<appl(<list>)>", &name[0], &t[3]));
	test_assert("match", 33, ATisEqual(t[3], t[1]));
	test_assert("match", 34, ATmatch(ATparse("rec-do(signature([1,2,3]))"),
																	 "rec-do(signature(<term>))", &t[4]));
	test_assert("match", 35, ATisEqual(t[1], t[4]));
	test_assert("match", 36, ATmatch((ATerm)ATempty, "[]"));

  printf("match tests ok.\n");
}

/*}}}  */
/*{{{  void testPrintf(void) */
void testPrintf()
{
	/* Outcommented. Have to find a way to test this w/o spamming
	 * stderr. Just print "printf ok"
	int i=14;
	ATfprintf(stderr, "Test: %3.4f\n", 2.345);
	ATfprintf(stderr, "%c%c%%%c%c\n", 't', 'e', 's', 't');
	ATfprintf(stderr, "%c%%%10s\n", 'T', "def");
	ATfprintf(stderr, "%10s %+5.3d\n", "abc", i);
	ATfprintf(stderr, "Pointer: %p (HexUpper: %X)\n", &i, (int)&i);
	*/
}
/*}}}  */
/*{{{  void testAnno(void) */

/**
  * Test annotations
  */

void testAnno(void)
{
  ATerm t[8];
  ATerm term, label, value, value2;

  term  = ATreadFromString("f(a)");
  label = ATreadFromString("label");
  value = ATreadFromString("value");
  value2= ATreadFromString("value2");
  t[0]  = ATsetAnnotation(term, label, value);
  t[1]  = ATsetAnnotation(term, label, value);
  t[2]  = ATsetAnnotation(term, label, value2);
  t[3]  = ATgetAnnotation(t[1], label);
  test_assert("anno", 1, ATisEqual(t[3], value));
  t[4] = ATsetAnnotation(t[1], label, value2);
  test_assert("anno", 2, ATisEqual(ATgetAnnotation(t[4], label), value2));

  test_assert("anno", 3, ATisEqual(t[0], t[1]));
  test_assert("anno", 4, !ATisEqual(t[0], t[2]));

  t[4] = ATremoveAnnotation(t[4], label);
  test_assert("anno", 5, ATgetAnnotation(t[4], label) == NULL);

  test_assert("anno", 6, ATisEqual(ATremoveAnnotation(t[0], label), term));

  printf("annotation tests ok.\n");
}

/*}}}  */
/*{{{  void testGC() */

void testGC()
{
	ATerm t[16];

	t[0] = ATparse("abc");
	t[1] = ATparse("f(abc)");
	t[2] = ATparse("g(<int>, [3,4])");
	t[3] = ATparse("a(3,4,5){<annotation>}");
	t[4] = t[3]+1;
	t[5] = (ATerm) ((char *) t[1] + 1);
	t[6] = (ATerm)NULL;
	t[7] = (ATerm)testGC;
	t[8] = (ATerm)t;
	t[9] = (ATerm)"Just a test!";
	t[10] = (ATerm)((char *)t[2]-1);
	t[11] = ATsetAnnotation(t[1], t[0], t[3]);
	t[12] = ATparse("[abc,f(abc)]");

	AT_collect(2);

	test_assert("gc", 0, AT_isValidTerm(t[0]));
	test_assert("gc", 1, AT_isValidTerm(t[1]));
	test_assert("gc", 2, AT_isValidTerm(t[2]));
	test_assert("gc", 3, AT_isValidTerm(t[3]));
	test_assert("gc", 4, !AT_isValidTerm(t[4]));
	test_assert("gc", 5, !AT_isValidTerm(t[5]));
	test_assert("gc", 6, !AT_isValidTerm(t[6]));
	test_assert("gc", 7, !AT_isValidTerm(t[7]));
	test_assert("gc", 8, !AT_isValidTerm(t[8]));
	test_assert("gc", 9, !AT_isValidTerm(t[9]));
	test_assert("gc", 10, !AT_isValidTerm(t[10]));
	test_assert("gc", 11, AT_isValidTerm(t[11]));
	
	AT_markTerm(t[12]);
	test_assert("gc-mark", 0, IS_MARKED(t[0]->header));
	test_assert("gc-mark", 1, IS_MARKED(t[1]->header));
	test_assert("gc-mark", 2, IS_MARKED(t[12]->header));
	test_assert("gc-mark", 3, !IS_MARKED(t[2]->header));
	test_assert("gc-mark", 4, AT_isMarkedSymbol(ATgetSymbol((ATermAppl)t[0])));

	printf("gc tests ok.\n");	
}

/*}}}  */
/*{{{  void testMark() */

/**
	* Test the marking code.
	*/

void testMark()
{
	int i;
	ATerm zero = ATparse("zero");
	ATerm one  = ATparse("one");
  ATerm t1 = zero;
	ATerm t2 = one;
	ATerm result;

	for(i=0; i<100000; i++) {
		t1 = ATmake("succ(<int>,<term>)", i, t1);
		t2 = ATmake("succ(<int>,<term>)", i, t2);
	}
	result = ATmake("result(<term>,<term>)", t1, t2);
	
	AT_markTerm(result);
	test_assert("marking", 1, IS_MARKED(zero->header));
	test_assert("marking", 2, IS_MARKED(one->header));
	AT_unmarkTerm(result);
	test_assert("marking", 3, !IS_MARKED(zero->header));
	test_assert("marking", 4, !IS_MARKED(one->header));

	printf("mark tests ok.\n");
}

/*}}}  */
/*{{{  void testTable() */

/**
	* Test table routines.
	*/

void testTable()
{
	int i;
	ATermTable table;
	ATermList keys;
	ATerm key[1000];
	ATerm val[1000];

	table = ATtableCreate(2, 80);

	for(i=0; i<1000; i++) {
		key[i] = ATmake("<int>", i);
		val[i] = ATmake("f(<int>)", i);
		ATtablePut(table, key[i], val[i]);
	}

	for(--i; i>=0; i--)
		test_assert("table", 1+i, ATisEqual(ATtableGet(table, key[i]), val[i]));

  keys = ATtableKeys(table);
	for(i=0; i<1000; i++)
		test_assert("table", 1000+i, ATindexOf(keys, key[i], 0) >= 0);

	for(i=0; i<1000; i++)
		ATtableRemove(table, key[i]);

	for(--i; i>=0; i--)
		test_assert("table", 2000+i, ATtableGet(table, key[i]) == NULL);

	ATtableDestroy(table);

	printf("table tests ok.\n");
}

/*}}}  */

void testBaffle()
{
	test_assert("baffle", 1, AT_calcUniqueSubterms(ATparse("f(a,[1])")) == 4);
	printf("baffle tests ok.\n");
}

/*{{{  int main(int argc, char *argv[]) */

/**
  * Test some features of the aterm library.
  */

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;

  ATinit(argc, argv, &bottomOfStack);

  testAlloc();
  testSymbol();
  testAppl();
  testList();
  testOther();
  testRead();
  testDict();
  testPrintf();
  testAnno();
  testMake();
  testMatch();
  testBaffle();
  testGC();
  testMark();
  testTable();

  return 0;
}

/*}}}  */
