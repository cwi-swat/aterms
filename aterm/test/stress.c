/*{{{  includes */

#ifdef WIN32
#include <stdlib.h>
#include <process.h>
#else
#include <unistd.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "_aterm.h"
#include "memory.h"
#include "afun.h"
#include "util.h"
#include "gc.h"

#include "stress_dict.h"

/*}}}  */
/*{{{  defines */

#define test_assert(cat,id,cond) if(!(cond)) test_failed(cat, id)

/*}}}  */
/*{{{  globals */

char stress_id[] = "$Id$";

#ifdef ABORT_ON_PARSE_ERROR
ATbool parse_error_encountered = ATfalse
#endif

/*}}}  */

/*{{{  void abort_handler(const char *format, va_list args) */

#ifdef ABORT_ON_PARSE_ERROR
void abort_handler(const char *format, va_list args)
{
  parse_error_encountered = ATtrue;
}
#endif
/*}}}  */

/*{{{  void test_failed(char *category, int id) */

/**
 * A test has failed.
 */

void test_failed(char *category, int id)
{
  fprintf(stderr, "%s test %d failed!\n", category, id);
  abort();
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

  fprintf(stdout, "Allocating 18 nodes of size 3:\n");
  for(i=0; i<18; i++) {
    test = AT_allocate(3);
    if(test)
      fprintf(stdout, "Result: %p\n", test);
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

#define NR_TEST_AFUNS (3*65535/2)
void
testSymbol(void)
{
  int i;
  Symbol symmies[8];
  AFun test_afuns[NR_TEST_AFUNS];
  char buf[BUFSIZ];

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

  for (i=0; i<NR_TEST_AFUNS; i++) {
    sprintf(buf, "xxx%d", i);
    test_afuns[i] = ATmakeSymbol(buf, 0, ATtrue);
  }

  for (i=0; i<NR_TEST_AFUNS; i++) {
    sprintf(buf, "xxx%d", i);
    test_assert("symbol", i, test_afuns[i] == ATmakeSymbol(buf, 0, ATtrue));
  }

  printf("symbol tests ok.\n");
}

/*}}}  */
/*{{{  void testBlob() */

static ATbool destructor_false_count = 0;
static ATbool destructor_true_count = 0;

static ATbool blob_destructor_false(ATermBlob blob)
{
  destructor_false_count++;
  return ATfalse;
}

static ATbool blob_destructor_true(ATermBlob blob)
{
  destructor_true_count++;
  free(ATgetBlobData(blob));
  return ATtrue;
}

void testBlob()
{
  char *ptr;
  static ATermBlob b[10];
  FILE *file;

  b[0] = ATmakeBlob(4, strdup("abc"));

  ptr = ATwriteToString((ATerm)b[0]);
  b[1] = (ATermBlob)ATparse(ptr);

  test_assert("blob", 1, strcmp(ATgetBlobData(b[0]), ATgetBlobData(b[1])) == 0);

  /*file = tmpfile();*/
  file = fopen("test.blob", "wb+");
  ATwriteToTextFile((ATerm)b[0], file);
  fflush(file);
  fseek(file, 0, SEEK_SET);
  b[2] = (ATermBlob)ATreadFromTextFile(file);
  test_assert("blob", 1, strcmp(ATgetBlobData(b[0]), ATgetBlobData(b[2])) == 0);

  ATregisterBlobDestructor(blob_destructor_false);
  ATregisterBlobDestructor(blob_destructor_false);
  ATregisterBlobDestructor(blob_destructor_true);
  ATregisterBlobDestructor(blob_destructor_false);

  b[0] = NULL;
  b[1] = NULL;
  b[2] = NULL;

  AT_collect(2);
  ATfprintf(stderr, "destr_false_count=%d, destr_true_count=%d\n",
	    destructor_false_count, destructor_true_count);
  assert(destructor_false_count == 2*destructor_true_count);

  assert(b[0] == NULL && b[1] == NULL && b[2] == NULL);

  ATunregisterBlobDestructor(blob_destructor_false);
  ATunregisterBlobDestructor(blob_destructor_true);
  ATunregisterBlobDestructor(blob_destructor_false);
  ATunregisterBlobDestructor(blob_destructor_false);

  fclose(file);
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
  char *data = strdup("123456789");

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

  assert(ATisEqual(apples[6], apples[1]));
  assert(ATisEqual(apples[1], apples[3]));
  assert(!ATisEqual(apples[2], apples[1]));
  assert(!ATisEqual(apples[2], apples[6]));
  assert(!ATisEqual(apples[1], apples[2]));
  assert(!ATisEqual(apples[2], apples[3]));
  assert(!ATisEqual(apples[0], apples[1]));

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

  test_assert("list-ops",18, ATisEqual(ATgetArguments(ATmakeAppl(ATmakeSymbol("f",2,ATfalse),
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

  ATfprintf(stdout, "result of ATremoveElement: %t\n", 
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

  test_assert("list-ops", 24,
	      ATisEqual(ATgetTail((ATermList)ATparse("[1,2,3,4]"),2), ATparse("[3,4]")));
  test_assert("list-ops", 25,
	      ATisEqual(ATgetTail((ATermList)ATparse("[1,2,3,4]"),-2), ATparse("[3,4]")));

  test_assert("list-ops", 26,
	      ATisEqual(ATreplaceTail((ATermList)ATparse("[1,2,3,4]"),
				      (ATermList)ATparse("[c,d]"), 2),
			ATparse("[1,2,c,d]")));

  test_assert("list-ops", 27,
	      ATisEqual(ATsort((ATermList)ATparse("[3,2,1]"), ATcompare),
			ATparse("[1,2,3]")));

  test_assert("list-ops", 28,
	      ATisEqual(ATsort((ATermList)ATparse("[[1,2],[1]]"), ATcompare),
			ATparse("[[1],[1,2]]")));

  test_assert("list-ops", 29,
	      ATisEqual(ATsort((ATermList)ATparse("[c,f(b,1),a,b,f(b),1.3,1.2]"),
			       ATcompare),
			ATparse("[a,b,c,f(b),f(b,1),1.2,1.3]")));

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
  FILE *f = fopen(SRCDIR "test.trms", "rb");
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
  t = ATreadFromString("f()");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("f(a,b,<123>,0.456,\"f\")");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("[]");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("f{[a,1],[b,ab{[1,2]}]}");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("<int>");
  ATfprintf(stdout, "read from string: %t\n", t);
  t = ATreadFromString("\"quoted: \\\"abc\\\"\"");
  ATfprintf(stdout, "read from string: %t\n", t);

#ifdef ABORT_ON_PARSE_ERROR
  ATsetAbortHandler(abort_handler);
  parse_error_encountered = ATfalse;
#endif
  fprintf(stderr, "Next term should give a parse error at line 0, col 17\n");
  f = fopen(SRCDIR "error.trm", "rb");
  t = ATreadFromTextFile(f);
  fclose(f);
#ifdef ABORT_ON_PARSE_ERROR
  ATsetAbortHandler(NULL);
  assert(parse_error_encountered);
#endif
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
  char *data = strdup("ABCDEFG");
  Symbol sym[8];
  ATerm t;

  t = ATparse("rec-eval(create-rule(\"Tcl/Tk-17097\",process-list(step),step,true,"
	      "[break,disable]))");

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
					  "echt", 123, 
					  ATmakeList2((ATerm)ATmakeInt(7), 
						      (ATerm)ATmakeReal(7.01))),
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

  test_assert("make", 9,
	      ATisEqual(ATmake("f(<int>,<int>,<int>,<int>,<int>,"
			       "<int>,<int>,<int>,<int>)", 
			       1, 2, 3, 4, 5, 6, 7, 8, 9),
			ATmakeAppl(ATmakeSymbol("f", 9, ATfalse),
				   ATmakeInt(1), ATmakeInt(2), ATmakeInt(3),
				   ATmakeInt(4), ATmakeInt(5), ATmakeInt(6),
				   ATmakeInt(7), ATmakeInt(8), ATmakeInt(9))));

  test_assert("make", 10, ATisEqual(ATmake("[\"f\"([<list>])]", 
					   ATparse("[1,2,3]")),
				    ATparse("[\"f\"([1,2,3])]")));

  fprintf(stderr, "The following two tests should generate parse errors.\n");
#ifdef ABORT_ON_PARSE_ERROR
  ATsetAbortHandler(abort_handler);
  parse_error_encountered = ATfalse;
#endif
  ATparse("<int");
  ATparse("f(<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>,<int>asdfaksdjfhasjkhf)");
#ifdef ABORT_ON_PARSE_ERROR
  ATsetAbortHandler(NULL);
  assert(parse_error_encountered);
#endif
  printf("make tests ok.\n");
}

/*}}}  */
/*{{{  void testTuple() */

void testTuple()
{
  test_assert("tuple", 1, ATisEqual(ATmakeAppl2(ATmakeSymbol("", 2, ATfalse),
						ATparse("a"), ATparse("b")),
				    ATparse("(a,b)")));

  test_assert("tuple", 2, AT_calcTextSize(ATparse("()")) == 2);
  test_assert("tuple", 3, strcmp(ATwriteToString(ATparse("()")), "()") == 0);
  test_assert("tuple", 4, AT_calcTextSize(ATparse("f()")) == 1);
  test_assert("tuple", 5, strcmp(ATwriteToString(ATparse("f()")), "f") == 0);
  test_assert("tuple", 6, AT_calcTextSize(ATparse("\"\"()")) == 2);
  test_assert("tuple", 7, strcmp(ATwriteToString(ATparse("\"\"()")),
				 "\"\"") == 0);
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
  char *name[3];
  int size;
  void *data;
  ATermList list;

  t[0] = ATmake("f(1,3.14,<placeholder>,a,\"b\",<term>)", 
		ATmake("type"), (ATerm)ATmakeBlob(4,strdup("abc")));
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


  test_assert("match", 19, ATmatch(t[0],
				   "<appl(1,<real>,<term>,<id>,<appl(<list>)>,<term>)>",
				   &name[0], &r,   &t[7], &name[1], &name[2], &list, &t[6]));
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
  ATerm emptylist;

  emptylist = AT_setAnnotations((ATerm)ATempty, ATparse("[[1,2]]"));
  test_assert("anno", 0, ATisEmpty(emptylist));

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

  test_assert("anno", 2, ATisEqual(t[4], t[2]));

  test_assert("anno", 3, ATisEqual(ATgetAnnotation(t[4], label), value2));

  test_assert("anno", 4, ATisEqual(t[0], t[1]));
  test_assert("anno", 5, !ATisEqual(t[0], t[2]));

  t[4] = ATremoveAnnotation(t[4], label);
  test_assert("anno", 6, ATgetAnnotation(t[4], label) == NULL);

  test_assert("anno", 7, ATisEqual(ATremoveAnnotation(t[0], label), term));

  t[5] = ATparse("test-anno{[label,unique_anno(42)]}");
  test_assert("anno", 8, ATgetAnnotation(t[5], ATparse("label")) != NULL);
  AT_collect(2);
  test_assert("anno", 9, ATisEqual(ATgetAnnotation(t[5],ATparse("label")),
				   ATparse("unique_anno(42)")));
  test_assert("anno", 10, ATisEqual(ATremoveAllAnnotations(t[0]), term));

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
  t[7] = (ATerm)((MachineWord)testGC);
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

#ifndef NO_SHARING
  AT_markTerm(t[12]);
  test_assert("gc-mark", 0, IS_MARKED(t[0]->header));
  test_assert("gc-mark", 1, IS_MARKED(t[1]->header));
  test_assert("gc-mark", 2, IS_MARKED(t[12]->header));
  test_assert("gc-mark", 3, !IS_MARKED(t[2]->header));
  test_assert("gc-mark", 4, AT_isMarkedSymbol(ATgetSymbol((ATermAppl)t[0])));
  AT_unmarkTerm(t[12]);
#endif

  printf("gc tests ok.\n");	
}

/*}}}  */
/*{{{  void testProtect() */

void testProtect()
{
  static ATerm ts1 = NULL, ts2 = NULL;
  static ATerm ts[2];

  ATprotect(&ts1);
  ts1 = ATmake("unique-1");
  ts2 = ATmake("unique-2");

  AT_collect(2);

  assert(AT_isValidTerm(ts1));

  ATunprotect(&ts1);
  AT_collect(2);

  ts[0] = (ATerm)"garbage";
  ts[1] = ATmake("unique-3");
  assert(!AT_isValidTerm(ts[0]));
  assert(AT_isValidTerm(ts[1]));
  assert(AT_isInsideValidTerm(ts[0]) == NULL);
  assert(AT_isInsideValidTerm((ATerm)(((char *)ts[1])+4)) != NULL);
  ATprotectMemory((void *)ts, sizeof(ATerm)*2);
  AT_collect(2);
  assert(AT_isValidTerm(ts[1]));
  ATunprotectMemory((void *)ts);
  AT_collect(2);
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
  ATerm t1 = NULL;
  ATerm t2 = NULL;
  ATerm result = NULL;

  t1 = zero;
  t2 = one;

  for(i=0; i<100000; i++) {
    t1 = ATmake("succ(<int>,<term>)", i, t1);
    t2 = ATmake("succ(<int>,<term>)", i, t2);
  }
  result = ATmake("result(<term>,<term>)", t1, t2);
  /*AT_assertUnmarked(result); Needs stacksize >> 8M */

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

/*{{{  void testTable2() */

/**
 * Table tests devised by JFG
 */

#define MAX_ELEM 50000 

void testTable2()
{ 
  ATermTable table;
  int i;

  table=ATtableCreate(2,75);

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    ATtablePut(table,ATmake("f(<int>)",i),ATmake("g(f(<int>),<int>)",i,i+1));
  }

  /* insert elements twice, as this caused previous versions
     of the hashtable to crash */
  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    ATtablePut(table,ATmake("f(<int>)",i),ATmake("g(f(<int>),<int>)",i,i+1));
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    if (ATtableGet(table,ATmake("f(<int>)",i))==NULL)
      ATerror("Problem1\n");
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    ATtableRemove(table,ATmake("f(<int>)",i));
  }

  if (ATgetLength(ATtableKeys(table))!=0)
    ATerror("Problem2\n");

  if (ATgetLength(ATtableValues(table))!=0)
  {    
    ATerror("Problem3\n");
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  {
    if (ATtableGet(table,ATmake("f(<int>)",i))!=NULL)
      ATerror("Problem4\n");
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  {
    ATtablePut(table,ATmake("f(<int>)",i+MAX_ELEM),ATmake("g(f(<int>),<int>)",i,i+1));
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    ATtablePut(table,ATmake("f(<int>)",i+MAX_ELEM),
	       ATmake("g(f(<int>),<int>)",i,i+2));
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    if (ATtableGet(table,ATmake("f(<int>)",i+MAX_ELEM))!=
	ATmake("g(f(<int>),<int>)",i,i+2))
      ATerror("Problem5\n");
  }

  if (ATgetLength(ATtableKeys(table))!=MAX_ELEM)
  { 
    ATerror("Problem6\n");
  }

  if (ATgetLength(ATtableValues(table))!=MAX_ELEM)
    ATerror("Problem7\n");
}

/*}}}  */

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

  testTable2();

  printf("table tests ok.\n");
}

/*}}}  */
/*{{{  void testIndexedSet() */

/**
 * Some of these tests have been devised by JFG
 */

void testIndexedSet()
{
  ATermIndexedSet set;
  int i;
  ATbool new;

  set=ATindexedSetCreate(2,75);

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    ATindexedSetPut(set,ATmake("f(<int>)",i),&new);
    if (!new) ATerror("Problem0.1");
  }

  /* insert elements twice, as this caused previous versions
     of the hashset to crash */
  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    ATindexedSetPut(set,ATmake("f(<int>)",i),&new);
    if (new) ATerror("Problem0.2");
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    if (ATindexedSetGetIndex(set,ATmake("f(<int>)",i))<0)
      ATerror("Problem1\n");
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    ATindexedSetRemove(set,ATmake("f(<int>)",i));
  }

  if (ATgetLength(ATindexedSetElements(set))!=0)
    ATerror("Problem2\n");

  for(i=0 ; i<MAX_ELEM ; i++)
  {
    if (ATindexedSetGetIndex(set,ATmake("f(<int>)",i))>=0)
      ATerror("Problem4\n");
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    ATindexedSetPut(set,ATmake("f(<int>)",i+MAX_ELEM),&new);
    if (!new) ATerror("Problem4.5");
  }

  for(i=0 ; i<MAX_ELEM ; i++)
  { 
    if (ATindexedSetGetIndex(set,ATmake("f(<int>)",i+MAX_ELEM))<0)
      ATerror("Problem5\n");
  }

  if (ATgetLength(ATindexedSetElements(set))!=MAX_ELEM)
    ATerror("Problem6\n");

  printf("indexedSet tests ok.\n");
}

/*}}}  */
/*{{{  void testDictToC() */

void testDictToC()
{
  init_stress_dict();
  test_assert("dicttoc", 1, ATisEqual(patternA, 
				      ATparse("patroon(\"a\",<term>)")));
  test_assert("dicttoc", 2, ATisEqual(patternB, ATparse("patroon(\"b\")")));
  test_assert("dicttoc", 3, symbolA == ATmakeAFun("A very complex symbol", 
						  3, ATtrue));
  test_assert("dicttoc", 4, symbolB == ATmakeAFun("another \"complex\" symbol",
						  0, ATtrue));
  printf("dicttoc tests ok.\n");
}

/*}}}  */
/*{{{  void testTBLegacy() */

void testTBLegacy()
{
  ATerm t[4];

  t[0] = ATparse("logpoint([Mid$INIT:logger?],[[Mid$INIT:logger?,logger(0)]])");
  t[1] = ATparse("logpoint([Mid$INIT{[type,logger{[result,true]}]}],"
		 "[[Mid$INIT{[type,logger{[result,true]}]},logger(0)]])");

  test_assert("TB-legacy", 1, ATisEqual(t[0],t[1]));

  printf("TB legacy tests ok.\n");
}

/*}}}  */
/*{{{  void testBaffle() */

void testBaffle()
{
  char buf[BUFSIZ], *ptr;
  FILE *file;
  ATerm test2, test1 = ATparse("f(1,a,<abc>,[24,g]{[a,b]})");
  int len = 0;

  test_assert("baffle", 1, AT_calcUniqueSubterms(ATparse("f(a,[1])"))==5);
  sprintf(buf, "baffle-test-%d.baf", (int)getpid());
  file = fopen(buf, "wb");
  if(file) {
    test_assert("baffle", 2, ATwriteToBinaryFile(test1, file));
    fclose(file);
    file = fopen(buf, "rb");
    test2 = ATreadFromBinaryFile(file); 
    test_assert("baffle", 3, test2);
    test_assert("baffle", 4, ATisEqual(test1, test2));
    fclose(file);
    unlink(buf);
  } else {
    fprintf(stderr, "warning could not open file: %s for writing.\n", buf);
  }

  ptr = ATwriteToBinaryString(test1, &len);
  ATfprintf(stderr, "term written to binary string: %t, size=%d\n", test1, len);
  test2 = ATreadFromBinaryString(ptr, len);
  ATfprintf(stderr, "term read from binary string : %t\n", test2);
  test_assert("baffle", 5, ATisEqual(test1, test2));

  printf("baffle tests ok.\n");
}

/*}}}  */
/*{{{  void testTaf() */

void testTaf()
{
  ATerm t[8];
  char *ptr;
  int len;
  FILE *file;

  char *input = "f(\"Lang functiesymbool\",\"Lang functiesymbool2\","
    "\"Lang functiesymbool2\",\"Lang functiesymbool\")";
  char *expected = "!f(\"Lang functiesymbool\",\"Lang functiesymbool2\",#B,#A)";

  t[0] = ATparse(input);
  t[1] = ATmake("g(<term>,x,<term>)", t[0], t[0]);

  t[2] = ATreadFromString(input);

  ptr = ATwriteToSharedString(t[0], &len);
  t[3] = ATreadFromSharedString(ptr, len);
  t[4] = ATparse("<ph(<ph2>)>");
  t[5] = ATparse("[]");
  t[6] = ATmake("h(<term>,1,2,1,3.14,[<term>,<term>,<term>],<term>)", 
		t[1], t[4],t[4], t[5], t[5]);

#ifndef NO_SHARING
  test_assert("taf", 0, strcmp(ptr, expected) == 0);
  test_assert("taf", 1, len == strlen(expected));
#endif
  test_assert("taf", 2, ATisEqual(t[0], t[2]));
  test_assert("taf", 3, ATisEqual(t[3], t[0]));

  file = fopen("test.taf", "w");
  if (!file) {
    ATerror("could not open file: test.taf for writing\n");
  }

  test_assert("taf", 4, ATwriteToSharedTextFile(t[6], file) >= 0);

  fclose(file);

  file = fopen("test.taf", "rb");
  if (!file) {
    ATerror("could not open file: test.taf for reading\n");
  }

  t[7] = ATreadFromSharedTextFile(file);
  test_assert("taf", 5, ATisEqual(t[6], t[7]));

  fclose(file);
  /*unlink("test.taf");*/

  printf("taf tests ok.\n");
}

/*}}}  */
/*{{{  void testChecksum() */

void testChecksum()
{
  ATerm t = ATparse("f(a,b,1,2,[])");
  char expected_digest[16] = { 0xf0, 0xbb, 0xaf, 0x3d, 0x93, 0xfa, 0x08, 0x2c,
    0xb7, 0xfe, 0xa9, 0x79, 0x6c, 0xd5, 0xdd, 0xdd };

    test_assert("checksum", 0, memcmp(expected_digest, ATchecksum(t), 16) == 0);

    printf("checksum tests ok.\n");
}

/*}}}  */
/*{{{  void testDiff() */

void testDiff()
{
  ATerm t1 = ATparse("f(a)");
  ATerm t2 = ATparse("f(b)");
  ATerm diffs;
  ATerm template;

  test_assert("diff", 0, ATdiff(t1, t2, &template, &diffs));
  test_assert("diff", 1, ATisEqual(template, ATparse("f(<diff-appls>)")));
  test_assert("diff", 2, ATisEqual(diffs, ATparse("[diff(a,b)]")));

  diffs = NULL;
  test_assert("diff", 3, ATdiff(t1, t2, NULL, &diffs));
  test_assert("diff", 4, ATisEqual(diffs, ATparse("[diff(a,b)]")));

  template = NULL;
  test_assert("diff", 5, ATdiff(t1, t2, &template, NULL));
  test_assert("diff", 6, ATisEqual(template, ATparse("f(<diff-appls>)")));

  test_assert("diff", 7, ATdiff(t1, t2, NULL, NULL));

  t1 = ATparse("[<f(4,3.14)>,[] ,[a,b],[e,f]]");
  t2 = ATparse("[<f(5,3.14)>,[1],7    ,[e]]");

  test_assert("diff", 0, ATdiff(t1, t2, &template, &diffs));

  /*ATfprintf(stderr, "template = %t, diffs = %t\n", template, diffs);*/

  test_assert("diff", 1, ATisEqual(template, 
				   ATparse("[<f(<diff-values>,3.140000)>,[<diff-lists>],<diff-types>,[e,<diff-lists>]]")));
  test_assert("diff", 2, ATisEqual(diffs, 
				   ATparse("[diff(4,5),diff([],[1]),diff([a,b],7),diff([f],[])]")));

  printf("diff tests ok.\n");
}

/*}}}  */

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
  testBlob();
  testOther();
  testRead();
  testDict();
  testPrintf();
  testAnno();
  testMake();
  testTuple();
  testMatch();
  testBaffle();
  testTaf();
  testGC();
  testProtect();
  testMark();
#ifndef NO_SHARING
  testTable();
  testIndexedSet();
#endif
  testDictToC();
  testTBLegacy();
  testChecksum();
#ifndef NO_SHARING
  testDiff();
#endif

  return 0;
}

/*}}}  */
