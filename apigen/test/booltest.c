
#include <stdio.h>
#include <assert.h>

#include "Booleans.h"

static void testBooleans(BoolList l)
{
  Bool b, left, right, bool[2];
  BoolElems elems;
  SDFWhiteSpace ws;
  Bool true;

  /* ATfprintf(stderr, "l=%t\n", l); */

  true = makeBoolTrue(ATparse("\"true\""));

  assert(isValidBoolList(l));
  assert(hasBoolListElems(l));

  elems = getBoolListElems(l);
  assert(isBoolElemsMany(elems));
  bool[0] = getBoolElemsHead(elems);
  b = bool[0];

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

  assert(hasBoolWsAfterAmpersand(b));
  ws = getBoolWsAfterAmpersand(b);
  assert(ATmatch(ws, "\" \""));

  bool[1] = makeBoolOr(makeBoolAnd(true,ws,ws,makeBoolFalse()),ws,ws,true);
  assert(ATisEqual(bool[0], bool[1]));

  elems = getBoolElemsTail(elems);
  assert(isBoolElemsSingle(elems));
  assert(!hasBoolElemsTail(elems));
}

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;
  ATerm term;

  ATinit(argc, argv, &bottomOfStack);
  initBooleansApi();

  term = ATreadFromNamedFile("booltest.af1");
  assert(term);
  ATprotect(&term);

  testBooleans(makeBoolListFromTerm(ATgetArgument((ATermAppl)term, 6)));

  return 0;
}
