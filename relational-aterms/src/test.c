
#include <stdio.h>
#include <aterm2.h>
#include "at-relation.h"

#define NON_PERSISTENT_COMPARE

int main(int argc, char **argv) {
  ATerm bottomOfStack;
  ATRelation relation;
  ATSet set1, set2;
  int i;
  ATinit(argc, argv, &bottomOfStack);
  ATR_init();

  relation = ATR_empty();
  set1 = ATR_empty();
  set2 = ATR_empty();

  for (i = 0; i < 1000; i++) {
    ATermInt n = ATmakeInt(i);
    ATermInt n1 = ATmakeInt(i+1);
    relation = ATR_insert(relation, ATR_makeTuple((ATerm)n, (ATerm)n1));
    /*set1 = ATR_insert(set1, (ATerm)n);
      set2 = ATR_insert(set2, (ATerm)n1);*/
  }

  relation = ATR_transitiveReflexiveClosure(relation);
  /*
  ATprintf("%t\n", ATR_getIterator(relation));
  ATprintf("Set1: %t\n", ATR_getIterator(set1));
  ATprintf("Set2: %t\n", ATR_getIterator(set2));
  ATprintf("Product: %t\n", ATR_getIterator(ATR_product(set1,set1)));
  */

  return 0;
}
