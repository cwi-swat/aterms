/*
 * $Id$
 */

#include <assert.h>
#include "bbtree.h"

const int dataValues[] = {
    5, 65, 34, 47, 975, 43, 3, 23, 53, 56, 86, 987, 54, 423, 0
};
const int sortedDataValues[] = {
    3, 5, 23, 34, 43, 47, 53, 54, 56, 65, 86, 423, 975, 987, 0
};

int intCompare(const ATerm aVal1, const ATerm aVal2)
{
  int x, y;

  x = ATgetInt((ATermInt)aVal1); y = ATgetInt((ATermInt)aVal2);
  if(x < y) return -1;
  if(x > y) return 1;
  return 0;
}

/* construct a new tree from scratch using the integer data values provided by
 * *pValues (until *pValues == 0)
 */
ATermBBTree makeTree(const int *pValues)
{
  ATermBBTree aTree;

  aTree = ATemptyBBTree;
  while(pValues && *pValues) {
    aTree = ATbbtreeInsert(aTree, (ATerm)ATmakeInt(*pValues), intCompare);
    ++pValues;
  }
  return aTree;
}

/* The compare function creates a sorted tree, so an in-order walk should
 * return the same set of values in increasing order */
void test_walk(ATermBBTree tree, const int *pValues)
{
  ATermBBTreeIterator iter;
  int i;

  iter = ATbbtreeIteratorInit(tree);
  
  while(!ATbbtreeIteratorAtEnd(iter) && *pValues) {
    i = ATgetInt((ATermInt)ATbbtreeIteratorValue(iter));
    assert(i == *pValues);
    iter = ATbbtreeIteratorAdvance(iter); ++pValues;
  }
  /* they should both end at the same time */
  assert(ATbbtreeIteratorAtEnd(iter));
  assert(! *pValues);
}

int main(int argc, char *argv[])
{
  ATerm aBottom;
  ATermBBTree aTree;

  ATinit(argc, argv, &aBottom);
  ATbbtreeInit();

  aTree = makeTree(dataValues);
  /*ATftreeToDot(stdout, aTree);*/

  test_walk(aTree, sortedDataValues);

  return 0;
}
