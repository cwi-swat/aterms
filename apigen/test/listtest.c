#include <stdio.h>
#include <assert.h>

#include "list.h"

static void testList()
{
  Module mod[2];
  Modules mods[3];
  Separated sep[7];
  NineSeps nine;

  Layout l1 = makeLayoutDefault("l1");
  Layout l2 = makeLayoutDefault("l2");
  Module m = makeModuleDefault("m");
  Module m1 = makeModuleDefault("m1");
  Module m2 = makeModuleDefault("m2");

  mods[0] = makeModulesFromTerm(ATparse("[\"m1\",\"m2\",\"m3\",\"m4\"]"));
  assert(isValidModules(mods[0]));

  mod[0] = getModulesHead(mods[0]);

  assert(hasModulesTail(mods[0]));
  mods[1] = getModulesTail(mods[0]);
  assert(isValidModules(mods[1]));

  mods[2] = getModulesTail(mods[1]);

  sep[0] = makeSeparatedEmpty();
  assert(getSeparatedLength(sep[0]) == 0);

  sep[1] = makeSeparatedSingle(m);
  assert(getSeparatedLength(sep[1]) == 1);

  sep[2] = concatSeparated(sep[0],l1,l2,sep[1]);
  sep[3] = appendSeparated(sep[0],l1,l2,m);
  assert(isEqualSeparated(sep[2],sep[3]));
  assert(getSeparatedLength(sep[2]) == 1);

  sep[5] = makeSeparatedMany(m1,l1,l2,makeSeparatedSingle(m2));
  sep[5] = concatSeparated(sep[5],l1,l2,sep[5]);
  sep[6] = makeSeparatedMany(m2,l2,l1,makeSeparatedSingle(m1));
  sep[6] = concatSeparated(sep[6],l2,l1,sep[6]);
  assert(isEqualSeparated(reverseSeparated(sep[5]),sep[6])); 
  assert(getSeparatedLength(sep[6]) == 4);
  assert(isEqualSeparated(SeparatedFromTerm(ATparse("[\"m2\",l(\"l2\"),\"sep\",l(\"l1\"),\"m1\",l(\"l2\"),\"sep\",l(\"l1\"),\"m2\",l(\"l2\"),\"sep\",l(\"l1\"),\"m1\"]")),sep[6]));

  /* Someone did not trust the proof of the length calculation
   * with separated lists. These tests should trigger any 
   * possible bug in it.
   */
  nine = makeNineSepsSingle(m);
  assert(getNineSepsLength(nine) == 1); 
  nine = concatNineSeps(nine,nine);  
  assert(getNineSepsLength(nine) == 2); 
  nine = concatNineSeps(nine,nine); 
  assert(getNineSepsLength(nine) == 4); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 5); 
  nine = concatNineSeps(nine,nine); 
  assert(getNineSepsLength(nine) == 10); 
  nine = concatNineSeps(nine,nine); 
  assert(getNineSepsLength(nine) == 20); 
  nine = concatNineSeps(nine,nine); 
  assert(getNineSepsLength(nine) == 40); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 41); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 42); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 43); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 44); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 45); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 46); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 47); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 48); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 49); 
  nine = makeNineSepsMany(m,nine);
  assert(getNineSepsLength(nine) == 50); 
}


int main(int argc, char *argv[])
{
  ATerm bottomOfStack;

  ATinit(argc, argv, &bottomOfStack);
  initListApi();

  testList();

  return 0;
}
