#include <stdio.h>
#include <assert.h>

#include "builtins.h"

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;

  ATinit(argc, argv, &bottomOfStack);
  initBuiltinsApi();

  ATerm data[20];

  /* Note that there is still a bug in ApiGen,
   * If all of these constructors were in the same sort,
   * wrong code would have been generated. This needs
   * to be fixed!
   */

  data[0] = (ATerm) makeIInteger(1);
  assert(data[0] && ATisEqual(data[0], ATparse("int(1)")));

  data[1] = (ATerm) makeDDouble(1.0);
  assert(data[1] && ATisEqual(data[1], ATparse("double(1.0)")));

  data[2] = (ATerm) makeSString("one");
  assert(data[2] && ATisEqual(data[2], ATparse("str(\"one\")")));

  data[3] = (ATerm) makeTTrm(ATparse("one"));
  assert(data[3] && ATisEqual(data[3], ATparse("term(one)")));

  data[4] = (ATerm) makeLLst((ATermList) ATparse("[one]"));
  assert(data[4] && ATisEqual(data[4], ATparse("list([one])")));


  return 0;
}
