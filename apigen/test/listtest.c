#include <stdio.h>
#include <assert.h>

#include "list.h"

static void testList()
{
  Module mod[2];
  Modules mods[3];

  mods[0] = makeModulesFromTerm(ATparse("modules([\"m1\",\"m2\",\"m3\",\"m4\"])"));
  assert(isValidModules(mods[0]));

  mod[0] = getModulesHead(mods[0]);

  assert(hasModulesTail(mods[0]));
  mods[1] = getModulesTail(mods[0]);
  assert(isValidModules(mods[1]));

  mods[2] = getModulesTail(mods[1]);
}


int main(int argc, char *argv[])
{
  ATerm bottomOfStack;

  ATinit(argc, argv, &bottomOfStack);
  initListApi();

  testList();

  return 0;
}
