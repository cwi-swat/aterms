
#include <stdio.h>
#include <string.h>
#include <assert.h>

#include "Booleans.h"

static void testBooleans(SDFBoolList l)
{
  SDFBool b, left, right, bool[2];
  SDFBoolElems elems;
  SDFLayout layout;
  SDFBool true;

  true = SDFmakeBoolTrue();

  /*ATfprintf(stderr, "l=%t\n", l);*/

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
  layout = SDFgetBoolWsAfterAmp(b);

  bool[1] = SDFmakeBoolOr(SDFmakeBoolAnd(true,layout,layout,SDFmakeBoolFalse()),
                          layout,layout,true);
  assert(ATisEqual(bool[0], bool[1]));

  elems = SDFgetBoolElemsTail(elems);
  assert(SDFisBoolElemsSingle(elems));
  assert(!SDFhasBoolElemsTail(elems));
}

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;
  ATerm term;
  PT_Tree tree;

  ATinit(argc, argv, &bottomOfStack);
  SDFinitBooleansApi();

  term = ATreadFromNamedFile("booltest.pt");
  assert(term);
  ATprotect(&term);

  tree = PT_getParseTreeTree(PT_ParseTreeFromTerm(term));
  testBooleans(SDFBoolListFromTerm(PT_TreeToTerm(tree)));

  return 0;
}
