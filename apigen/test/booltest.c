
#include <stdio.h>
#include <assert.h>

#include "Booleans.h"

static void testBooleans(Bool b)
{
  Bool left, right, bool[2];
  SDFWhiteSpace ws;

  /*ATfprintf(stderr, "b=%t\n", b);*/

  bool[0] = b;
  assert(isValidBool(b));
  assert(isBoolOr(b));

  assert(hasBoolLop(b));
  left = getBoolLop(b);
  assert(isValidBool(left));
  assert(isBoolAnd(left));

  assert(hasBoolRop(b));
  right = getBoolRop(b);
  assert(isValidBool(right));
  assert(isBoolTrue(right));
  
  b = left;
  assert(hasBoolLop(b));
  left = getBoolLop(b);
  assert(isValidBool(left));
  assert(isBoolTrue(left));
  
  assert(hasBoolRop(b));
  right = getBoolRop(b);
  assert(isValidBool(right));
  assert(isBoolFalse(right));

  assert(hasBoolWsAfterAnd(b));
  ws = getBoolWsAfterAnd(b);
  assert(ATmatch(ws, "\"\""));

  bool[1] = makeBoolOr(makeBoolAnd(makeBoolTrue(),ws,ws,makeBoolFalse()),ws,ws,makeBoolTrue());
  assert(ATisEqual(bool[0], bool[1]));
}

int main(int argc, char *argv[])
{
  FILE *f;
  ATerm bottomOfStack;
  ATerm term;

  ATinit(argc, argv, &bottomOfStack);
  initBooleansApi();

  term = ATreadFromNamedFile("booltest.af1");
  assert(term);
  ATprotect(&term);

  testBooleans(makeBoolFromTerm(ATgetArgument((ATermAppl)term, 6)));

  return 0;
}
