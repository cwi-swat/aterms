#include <stdio.h>
#include <assert.h>
#include <string.h>

#include "builtins.h"

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;

  ATerm data[20];
  ATinit(argc, argv, &bottomOfStack);
  initBuiltinsApi();


  /* Note that there is still a bug in ApiGen,
   * If all of these constructors were in the same sort,
   * wrong code would have been generated. This needs
   * to be fixed!
   */

  data[0] = (ATerm) makeDIinteger(1);
  assert(data[0] && ATisEqual(data[0], ATparse("int(1)")));

  data[1] = (ATerm) makeDDdouble(1.0);
  assert(data[1] && ATisEqual(data[1], ATparse("double(1.0)")));

  data[2] = (ATerm) makeDSstring("one");
  assert(data[2] && ATisEqual(data[2], ATparse("str(\"one\")")));

  data[3] = (ATerm) makeDTrm(ATparse("one"));
  assert(data[3] && ATisEqual(data[3], ATparse("term(one)")));

  data[4] = (ATerm) makeDLst((ATermList) ATparse("[one]"));
  assert(data[4] && ATisEqual(data[4], ATparse("list([one])")));

  data[5] = (ATerm) makeLexicalDefault("hello");
  assert(data[5] && ATisEqual(data[5], ATparse("string([104,101,108,108,111])")));
  assert(strcmp(getLexicalString((Lexical) data[5]), "hello") == 0);

  data[6] = (ATerm) makeCharacterDefault('A');
  assert(data[6] && ATisEqual(data[6], ATparse("character(65)")));
  assert(getCharacterCh((Character) data[6]) == 'A' );

  return 0;
}
