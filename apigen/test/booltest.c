
#include <stdio.h>
#include <assert.h>

#include "Booleans.h"

static void testBooleans(SDFBoolList l)
{
  SDFBool b, left, right, bool[2];
  SDFBoolElems elems;
  SDFLayout ws;
  SDFBool true;

  /* ATfprintf(stderr, "l=%t\n", l); */

  true = SDFmakeBoolTrue(ATparse("\"true\""));

  assert(SDFisValidBoolList(l));
  assert(SDFhasBoolListElems(l));

  elems = SDFgetBoolListElems(l);
  assert(SDFisBoolElemsMany(elems));
  bool[0] = SDFgetBoolElemsHead(elems);
  b = bool[0];

  assert(SDFisValidBool(b));
  assert(SDFisBoolOr(b));

  assert(SDFhasBoolLeft(b));
  left = SDFgetBoolLeft(b);
  assert(SDFisValidBool(left));
  assert(SDFisBoolAnd(left));

  assert(SDFhasBoolRight(b));
  right = SDFgetBoolRight(b);
  assert(SDFisValidBool(right));
  assert(SDFisBoolTrue(right));
  
  b = left;
  assert(SDFhasBoolLeft(b));
  left = SDFgetBoolLeft(b);
  assert(SDFisValidBool(left));
  assert(SDFisBoolTrue(left));
  
  assert(SDFhasBoolRight(b));
  right = SDFgetBoolRight(b);
  assert(SDFisValidBool(right));
  assert(SDFisBoolFalse(right));

  assert(SDFhasBoolWsAfterAmp(b));
  ws = SDFgetBoolWsAfterAmp(b);
  assert(ATmatch(ws, "\" \""));

  bool[1] = SDFmakeBoolOr(SDFmakeBoolAnd(true,ws,ws,SDFmakeBoolFalse()),ws,ws,true);
  assert(ATisEqual(bool[0], bool[1]));

  elems = SDFgetBoolElemsTail(elems);
  assert(SDFisBoolElemsSingle(elems));
  assert(!SDFhasBoolElemsTail(elems));
}

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;
  ATerm term;

  ATinit(argc, argv, &bottomOfStack);
  SDFinitBooleansApi();

  term = ATreadFromNamedFile("booltest.af1");
  assert(term);
  ATprotect(&term);

  testBooleans(SDFmakeBoolListFromTerm(ATgetArgument((ATermAppl)term, 6)));

  return 0;
}
