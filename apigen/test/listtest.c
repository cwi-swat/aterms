#include <stdio.h>
#include <assert.h>

#include "list.h"

static void testList()
{
  Module mod[2];
  Modules mods[3];
  ATerm t;

  mods[0] = makeModulesFromTerm(
    ATparse("modules(m1,m2,empty-modules)"));
    //ATparse("modules(m1,modules(m2,empty-modules))"));
  assert(isValidModules(mods[0]));

  mod[0] = getModulesFirst(mods[0]);

  assert(hasModulesNext(mods[0]));
  mods[1] = getModulesNext(mods[0]);
  assert(isValidModules(mods[1]));

  mods[2] = getModulesNext(mods[1]);
  assert(isModulesEmpty(mods[2]));
  assert(!hasModulesNext(mods[2]));
  assert(ATisEqual(mods[2], makeModulesEmpty()));
}


int main(int argc, char *argv[])
{
  ATerm bottomOfStack;

  ATinit(argc, argv, &bottomOfStack);
  initListApi();

  testList();

  return 0;
}
